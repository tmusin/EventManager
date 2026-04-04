package ru.musintimur.eventmanager.service

import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventRegistration
import ru.musintimur.eventmanager.domain.User

interface RegistrationService {
    fun findById(id: Long): EventRegistration

    fun getByEvent(eventId: Long): List<EventRegistration>

    fun getByUser(userId: Long): List<EventRegistration>

    fun isRegistered(
        eventId: Long,
        userId: Long,
    ): Boolean

    fun register(
        event: Event,
        user: User,
    ): EventRegistration

    fun cancel(
        eventId: Long,
        userId: Long,
    )

    fun rejectParticipant(registrationId: Long)

    fun markAsPaid(registrationId: Long)
}
