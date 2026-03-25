package ru.musintimur.eventmanager.service

import org.springframework.web.multipart.MultipartFile
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventPhoto
import ru.musintimur.eventmanager.domain.User

interface GalleryService {
    fun findById(id: Long): EventPhoto

    fun getByEvent(eventId: Long): List<EventPhoto>

    fun upload(
        event: Event,
        uploader: User,
        file: MultipartFile,
        caption: String?,
    ): EventPhoto

    fun delete(photo: EventPhoto)

    fun saveCoverImage(
        event: Event,
        file: MultipartFile,
    ): String
}
