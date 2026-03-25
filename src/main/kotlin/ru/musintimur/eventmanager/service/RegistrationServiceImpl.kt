package ru.musintimur.eventmanager.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventRegistration
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.domain.RegistrationStatus
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.EventRegistrationRepository

@Service
class RegistrationServiceImpl(
    private val registrationRepository: EventRegistrationRepository,
    private val eventService: EventService,
) : RegistrationService {
    @Transactional(readOnly = true)
    override fun findById(id: Long): EventRegistration =
        registrationRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Запись #$id не найдена") }

    @Transactional(readOnly = true)
    override fun getByEvent(eventId: Long): List<EventRegistration> = registrationRepository.findAllByEventId(eventId)

    @Transactional(readOnly = true)
    override fun getByUser(userId: Long): List<EventRegistration> = registrationRepository.findAllByUserId(userId)

    @Transactional(readOnly = true)
    override fun isRegistered(
        eventId: Long,
        userId: Long,
    ): Boolean = registrationRepository.existsByEventIdAndUserId(eventId, userId)

    /** Запись пользователя на мероприятие */
    @Transactional
    override fun register(
        event: Event,
        user: User,
    ): EventRegistration {
        require(event.status == EventStatus.APPROVED) {
            "Запись возможна только на опубликованное мероприятие"
        }
        require(!registrationRepository.existsByEventIdAndUserId(event.id, user.id)) {
            "Пользователь уже записан на это мероприятие"
        }
        require(eventService.hasAvailableSlots(event)) {
            "Свободных мест нет"
        }
        val registration = EventRegistration(event = event, user = user)
        return registrationRepository.save(registration)
    }

    /** Пользователь отменяет свою запись */
    @Transactional
    override fun cancel(
        eventId: Long,
        userId: Long,
    ) {
        val reg =
            registrationRepository
                .findByEventIdAndUserId(eventId, userId)
                ?: throw NoSuchElementException("Запись не найдена")
        registrationRepository.delete(reg)
    }

    /** Организатор отклоняет участника */
    @Transactional
    override fun rejectParticipant(registrationId: Long) {
        val reg = findById(registrationId)
        reg.status = RegistrationStatus.REJECTED
        registrationRepository.save(reg)
    }

    /** Организатор отмечает участника как оплатившего */
    @Transactional
    override fun markAsPaid(registrationId: Long) {
        val reg = findById(registrationId)
        reg.status = RegistrationStatus.PAID
        registrationRepository.save(reg)
    }
}
