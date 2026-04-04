package ru.musintimur.eventmanager.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.AbstractIntegrationTest
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.domain.User
import java.math.BigDecimal
import java.time.LocalDateTime

@Transactional
class EventRepositoryTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var organizer: User

    @BeforeEach
    fun setUp() {
        organizer =
            userRepository.save(
                User(username = "organizer_${System.nanoTime()}", password = "hashed"),
            )
    }

    private fun createEvent(status: EventStatus = EventStatus.PENDING): Event =
        eventRepository.save(
            Event(
                title = "Test Event",
                description = "Description",
                eventDate = LocalDateTime.now().plusDays(7),
                price = BigDecimal("100.00"),
                maxParticipants = 10,
                organizer = organizer,
                status = status,
            ),
        )

    @Test
    fun `findAllByStatus returns only approved events`() {
        createEvent(EventStatus.APPROVED)
        createEvent(EventStatus.APPROVED)
        createEvent(EventStatus.PENDING)

        val page = eventRepository.findAllByStatus(EventStatus.APPROVED, PageRequest.of(0, 10))

        assertTrue(page.content.size >= 2)
        page.content.forEach { assertEquals(EventStatus.APPROVED, it.status) }
    }

    @Test
    fun `findByIdAndStatus returns event with matching status`() {
        val event = createEvent(EventStatus.APPROVED)

        val found = eventRepository.findByIdAndStatus(event.id, EventStatus.APPROVED)

        assertEquals(event.id, found?.id)
    }

    @Test
    fun `findByIdAndStatus returns null for wrong status`() {
        val event = createEvent(EventStatus.PENDING)

        val found = eventRepository.findByIdAndStatus(event.id, EventStatus.APPROVED)

        assertNull(found)
    }

    @Test
    fun `findAllByOrganizerId returns organizer events`() {
        createEvent()
        createEvent()

        val events = eventRepository.findAllByOrganizerId(organizer.id)

        assertTrue(events.size >= 2)
        events.forEach { assertEquals(organizer.id, it.organizer.id) }
    }

    @Test
    fun `countActiveParticipants returns zero for event without registrations`() {
        val event = createEvent(EventStatus.APPROVED)

        val count = eventRepository.countActiveParticipants(event.id)

        assertEquals(0, count)
    }
}
