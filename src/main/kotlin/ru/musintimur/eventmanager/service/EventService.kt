package ru.musintimur.eventmanager.service

import org.springframework.data.domain.Page
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.User
import java.math.BigDecimal
import java.time.LocalDateTime

interface EventService {
    fun getApprovedPage(page: Int): Page<Event>

    fun getApprovedById(id: Long): Event

    fun findById(id: Long): Event

    fun getPendingEvents(): List<Event>

    fun getOrganizerEvents(organizerId: Long): List<Event>

    fun create(
        title: String,
        description: String,
        eventDate: LocalDateTime,
        price: BigDecimal,
        maxParticipants: Int,
        organizer: User,
        tempCoverPath: String? = null,
    ): Event

    fun update(
        event: Event,
        title: String,
        description: String,
        eventDate: LocalDateTime,
        price: BigDecimal,
        maxParticipants: Int,
    ): Event

    fun updateCoverImage(
        event: Event,
        path: String,
    ): Event

    fun approve(eventId: Long): Event

    fun reject(eventId: Long): Event

    fun markAsCompleted(event: Event): Event

    fun hasAvailableSlots(event: Event): Boolean
}
