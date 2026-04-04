package ru.musintimur.eventmanager.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventStatus

interface EventRepository : JpaRepository<Event, Long> {
    /** Главная страница: только одобренные мероприятия, сортировка по дате */
    @EntityGraph(attributePaths = ["organizer"])
    fun findAllByStatus(
        status: EventStatus,
        pageable: Pageable,
    ): Page<Event>

    /** Список мероприятий на рассмотрении у менеджера */
    @EntityGraph(attributePaths = ["organizer"])
    fun findAllByStatus(status: EventStatus): List<Event>

    /** Мероприятия конкретного организатора */
    @EntityGraph(attributePaths = ["organizer"])
    fun findAllByOrganizerId(organizerId: Long): List<Event>

    /** Поиск одобренного мероприятия по id (для публичного просмотра) */
    @EntityGraph(attributePaths = ["organizer"])
    fun findByIdAndStatus(
        id: Long,
        status: EventStatus,
    ): Event?

    /** Текущее число подтверждённых/оплативших участников мероприятия */
    @Query(
        """
        SELECT COUNT(r) FROM EventRegistration r
        WHERE r.event.id = :eventId
          AND r.status IN ('CONFIRMED', 'PAID')
        """,
    )
    fun countActiveParticipants(
        @Param("eventId") eventId: Long,
    ): Int
}
