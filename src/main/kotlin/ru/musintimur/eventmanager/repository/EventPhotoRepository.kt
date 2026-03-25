package ru.musintimur.eventmanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.musintimur.eventmanager.domain.EventPhoto

interface EventPhotoRepository : JpaRepository<EventPhoto, Long> {
    fun findAllByEventIdOrderByUploadedAtAsc(eventId: Long): List<EventPhoto>
}
