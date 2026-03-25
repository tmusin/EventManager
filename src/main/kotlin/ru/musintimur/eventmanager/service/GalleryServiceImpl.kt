package ru.musintimur.eventmanager.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventPhoto
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.EventPhotoRepository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

@Service
class GalleryServiceImpl(
    private val photoRepository: EventPhotoRepository,
    @Value("\${app.upload.path}") private val uploadPath: String,
) : GalleryService {
    @Transactional(readOnly = true)
    override fun findById(id: Long): EventPhoto =
        photoRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Фото #$id не найдено") }

    @Transactional(readOnly = true)
    override fun getByEvent(eventId: Long): List<EventPhoto> = photoRepository.findAllByEventIdOrderByUploadedAtAsc(eventId)

    @Transactional
    override fun upload(
        event: Event,
        uploader: User,
        file: MultipartFile,
        caption: String?,
    ): EventPhoto {
        require(event.status == EventStatus.COMPLETED) {
            "Фотографии можно добавлять только после завершения мероприятия"
        }
        val dir: Path = Paths.get(uploadPath, "events", event.id.toString(), "gallery")
        Files.createDirectories(dir)

        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val filename = "${UUID.randomUUID()}.$ext"
        Files.copy(file.inputStream, dir.resolve(filename))

        val relativePath = "events/${event.id}/gallery/$filename"
        val photo =
            EventPhoto(
                event = event,
                uploadedBy = uploader,
                filePath = relativePath,
                caption = caption,
            )
        return photoRepository.save(photo)
    }

    @Transactional
    @PreAuthorize("hasPermission(#photo, 'DELETE') or hasRole('MANAGER') or hasRole('ADMIN')")
    override fun delete(photo: EventPhoto) {
        val fullPath = Paths.get(uploadPath, photo.filePath)
        Files.deleteIfExists(fullPath)
        photoRepository.delete(photo)
    }

    /** Сохранение обложки мероприятия (вызывается из EventService) */
    override fun saveCoverImage(
        event: Event,
        file: MultipartFile,
    ): String {
        val dir: Path = Paths.get(uploadPath, "events", event.id.toString())
        Files.createDirectories(dir)

        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val filename = "cover.$ext"
        val target = dir.resolve(filename)
        Files.deleteIfExists(target)
        Files.copy(file.inputStream, target)

        return "events/${event.id}/$filename"
    }
}
