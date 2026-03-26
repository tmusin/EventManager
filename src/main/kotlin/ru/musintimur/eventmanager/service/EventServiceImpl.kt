package ru.musintimur.eventmanager.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.EventRepository
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class EventServiceImpl(
    private val eventRepository: EventRepository,
) : EventService {
    companion object {
        const val PAGE_SIZE = 10
    }

    // ── Публичные запросы ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    override fun getApprovedPage(page: Int): Page<Event> =
        eventRepository.findAllByStatus(
            EventStatus.APPROVED,
            PageRequest.of(page, PAGE_SIZE, Sort.by("eventDate").ascending()),
        )

    @Transactional(readOnly = true)
    override fun getApprovedById(id: Long): Event =
        eventRepository.findByIdAndStatus(id, EventStatus.APPROVED)
            ?: throw NoSuchElementException("Мероприятие #$id не найдено или не опубликовано")

    @Transactional(readOnly = true)
    override fun findById(id: Long): Event =
        eventRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Мероприятие #$id не найдено") }

    // ── Менеджерские запросы ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    override fun getPendingEvents(): List<Event> = eventRepository.findAllByStatus(EventStatus.PENDING)

    // ── Запросы организатора ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    override fun getOrganizerEvents(organizerId: Long): List<Event> = eventRepository.findAllByOrganizerId(organizerId)

    // ── Мутации ──────────────────────────────────────────────────────────────

    @Transactional
    override fun create(
        title: String,
        description: String,
        eventDate: LocalDateTime,
        price: BigDecimal,
        maxParticipants: Int,
        organizer: User,
    ): Event {
        val event =
            Event(
                title = title,
                description = description,
                eventDate = eventDate,
                price = price,
                maxParticipants = maxParticipants,
                organizer = organizer,
            )
        return eventRepository.save(event)
    }

    @Transactional
    override fun update(
        event: Event,
        title: String,
        description: String,
        eventDate: LocalDateTime,
        price: BigDecimal,
        maxParticipants: Int,
    ): Event {
        event.title = title
        event.description = description
        event.eventDate = eventDate
        event.price = price
        event.maxParticipants = maxParticipants
        event.updatedAt = LocalDateTime.now()
        return eventRepository.save(event)
    }

    @Transactional
    override fun updateCoverImage(
        event: Event,
        path: String,
    ): Event {
        event.coverImagePath = path
        event.updatedAt = LocalDateTime.now()
        return eventRepository.save(event)
    }

    /** Менеджер одобряет мероприятие */
    @Transactional
    override fun approve(eventId: Long): Event {
        val event = findById(eventId)
        event.status = EventStatus.APPROVED
        event.updatedAt = LocalDateTime.now()
        return eventRepository.save(event)
    }

    /** Менеджер отклоняет мероприятие */
    @Transactional
    override fun reject(eventId: Long): Event {
        val event = findById(eventId)
        event.status = EventStatus.REJECTED
        event.updatedAt = LocalDateTime.now()
        return eventRepository.save(event)
    }

    /** Организатор устанавливает статус "Мероприятие состоялось" */
    @Transactional
    override fun markAsCompleted(event: Event): Event {
        require(event.status == EventStatus.APPROVED) {
            "Завершить можно только одобренное мероприятие"
        }
        event.status = EventStatus.COMPLETED
        event.updatedAt = LocalDateTime.now()
        return eventRepository.save(event)
    }

    @Transactional(readOnly = true)
    override fun hasAvailableSlots(event: Event): Boolean {
        if (event.maxParticipants == 0) return true
        val active = eventRepository.countActiveParticipants(event.id)
        return active < event.maxParticipants
    }
}
