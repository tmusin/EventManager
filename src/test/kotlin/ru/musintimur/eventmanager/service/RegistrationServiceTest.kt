package ru.musintimur.eventmanager.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.AbstractIntegrationTest
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.domain.RegistrationStatus
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.EventRepository
import ru.musintimur.eventmanager.repository.UserRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Transactional
class RegistrationServiceTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var registrationService: RegistrationService

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var organizer: User
    private lateinit var participant: User
    private lateinit var approvedEvent: Event

    @BeforeEach
    fun setUp() {
        organizer = userRepository.save(User(username = "org_${System.nanoTime()}", password = "h"))
        participant = userRepository.save(User(username = "par_${System.nanoTime()}", password = "h"))
        approvedEvent =
            eventRepository.save(
                Event(
                    title = "Event",
                    description = "Desc",
                    eventDate = LocalDateTime.now().plusDays(1),
                    price = BigDecimal.ZERO,
                    maxParticipants = 3,
                    organizer = organizer,
                    status = EventStatus.APPROVED,
                ),
            )
    }

    @Test
    fun `register creates confirmed registration`() {
        val reg = registrationService.register(approvedEvent, participant)

        assertEquals(RegistrationStatus.CONFIRMED, reg.status)
        assertEquals(participant.id, reg.user.id)
    }

    @Test
    fun `register throws for duplicate registration`() {
        registrationService.register(approvedEvent, participant)

        assertThrows(IllegalArgumentException::class.java) {
            registrationService.register(approvedEvent, participant)
        }
    }

    @Test
    fun `register throws when no available slots`() {
        // Заполняем все 3 места
        repeat(3) { i ->
            val u = userRepository.save(User(username = "u${i}_${System.nanoTime()}", password = "h"))
            registrationService.register(approvedEvent, u)
        }

        val extra = userRepository.save(User(username = "extra_${System.nanoTime()}", password = "h"))
        assertThrows(IllegalArgumentException::class.java) {
            registrationService.register(approvedEvent, extra)
        }
    }

    @Test
    fun `cancel removes registration`() {
        registrationService.register(approvedEvent, participant)
        assertTrue(registrationService.isRegistered(approvedEvent.id, participant.id))

        registrationService.cancel(approvedEvent.id, participant.id)

        assertFalse(registrationService.isRegistered(approvedEvent.id, participant.id))
    }

    @Test
    fun `markAsPaid changes status to PAID`() {
        val reg = registrationService.register(approvedEvent, participant)

        registrationService.markAsPaid(reg.id)

        val updated = registrationService.findById(reg.id)
        assertEquals(RegistrationStatus.PAID, updated.status)
    }

    @Test
    fun `rejectParticipant changes status to REJECTED`() {
        val reg = registrationService.register(approvedEvent, participant)

        registrationService.rejectParticipant(reg.id)

        val updated = registrationService.findById(reg.id)
        assertEquals(RegistrationStatus.REJECTED, updated.status)
    }
}
