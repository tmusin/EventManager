package ru.musintimur.eventmanager.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.AbstractIntegrationTest
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.UserRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Transactional
class EventServiceTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var organizer: User

    @BeforeEach
    fun setUp() {
        organizer =
            userRepository.save(
                User(username = "org_${System.nanoTime()}", password = "hashed"),
            )
    }

    private fun createTestEvent() =
        eventService.create(
            title = "My Event",
            description = "Description",
            eventDate = LocalDateTime.now().plusDays(3),
            price = BigDecimal("500.00"),
            maxParticipants = 5,
            organizer = organizer,
        )

    @Test
    fun `create saves event with PENDING status`() {
        val event = createTestEvent()

        assertEquals(EventStatus.PENDING, event.status)
        assertEquals(organizer.id, event.organizer.id)
        assertTrue(event.id > 0)
    }

    @Test
    fun `approve changes status to APPROVED`() {
        val event = createTestEvent()

        val approved = eventService.approve(event.id)

        assertEquals(EventStatus.APPROVED, approved.status)
    }

    @Test
    fun `reject changes status to REJECTED`() {
        val event = createTestEvent()

        val rejected = eventService.reject(event.id)

        assertEquals(EventStatus.REJECTED, rejected.status)
    }

    @Test
    fun `markAsCompleted changes status to COMPLETED`() {
        val event = createTestEvent()
        eventService.approve(event.id)
        val approved = eventService.findById(event.id)

        val completed = eventService.markAsCompleted(approved)

        assertEquals(EventStatus.COMPLETED, completed.status)
    }

    @Test
    fun `markAsCompleted throws for non-approved event`() {
        val event = createTestEvent() // статус PENDING

        assertThrows(IllegalArgumentException::class.java) {
            eventService.markAsCompleted(event)
        }
    }

    @Test
    fun `hasAvailableSlots returns true when maxParticipants is 0`() {
        val event = createTestEvent()

        val unlimited =
            eventService.create(
                title = "Unlimited",
                description = "No limit",
                eventDate = LocalDateTime.now().plusDays(1),
                price = BigDecimal.ZERO,
                maxParticipants = 0,
                organizer = organizer,
            )

        assertTrue(eventService.hasAvailableSlots(unlimited))
    }

    @Test
    fun `getOrganizerEvents returns only this organizer events`() {
        createTestEvent()
        createTestEvent()

        val events = eventService.getOrganizerEvents(organizer.id)

        assertTrue(events.size >= 2)
        events.forEach { assertEquals(organizer.id, it.organizer.id) }
    }
}
