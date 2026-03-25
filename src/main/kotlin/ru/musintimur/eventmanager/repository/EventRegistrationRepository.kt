package ru.musintimur.eventmanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.musintimur.eventmanager.domain.EventRegistration
import ru.musintimur.eventmanager.domain.RegistrationStatus

interface EventRegistrationRepository : JpaRepository<EventRegistration, Long> {
    fun findAllByEventId(eventId: Long): List<EventRegistration>

    fun findAllByUserId(userId: Long): List<EventRegistration>

    fun findByEventIdAndUserId(
        eventId: Long,
        userId: Long,
    ): EventRegistration?

    fun existsByEventIdAndUserId(
        eventId: Long,
        userId: Long,
    ): Boolean

    fun countByEventIdAndStatusIn(
        eventId: Long,
        statuses: List<RegistrationStatus>,
    ): Int
}
