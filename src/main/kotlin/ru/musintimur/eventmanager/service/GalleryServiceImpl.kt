package ru.musintimur.eventmanager.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventPhoto
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
    private val log: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    @Transactional(readOnly = true)
    override fun findById(id: Long): EventPhoto =
        photoRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Фото #$id не найдено") }

    @Transactional(readOnly = true)
    override fun getByEvent(eventId: Long): List<EventPhoto> = photoRepository.findAllByEventIdOrderByUploadedAtAsc(eventId)

    /**
     * Сохраняет файл на диск и возвращает относительный путь.
     * Намеренно НЕ @Transactional — файловая система не транзакционна.
     * Вызывается ДО savePhotoRecord. При ошибке записи в БД
     * вызывающий код обязан удалить файл через deleteFileQuietly().
     */
    override fun savePhotoFile(
        event: Event,
        file: MultipartFile,
    ): String {
        val dir: Path = Paths.get(uploadPath, "events", event.id.toString(), "gallery")
        Files.createDirectories(dir)
        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val filename = "${UUID.randomUUID()}.$ext"
        Files.copy(file.inputStream, dir.resolve(filename))
        return "events/${event.id}/gallery/$filename"
    }

    /**
     * Сохраняет запись о фото в БД. Вызывается ПОСЛЕ успешного savePhotoFile.
     * При исключении транзакция откатывается — вызывающий код должен
     * удалить файл через deleteFileQuietly().
     */
    @Transactional
    override fun savePhotoRecord(
        event: Event,
        uploader: User,
        filePath: String,
        caption: String?,
    ): EventPhoto {
        val photo =
            EventPhoto(
                event = event,
                uploadedBy = uploader,
                filePath = filePath,
                caption = caption,
            )
        return photoRepository.save(photo)
    }

    /**
     * Сохраняет обложку во временную папку.
     * Используется до того, как id мероприятия известен.
     */
    override fun saveTempCoverImage(file: MultipartFile): String {
        val dir: Path = Paths.get(uploadPath, "temp")
        Files.createDirectories(dir)
        val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val filename = "${UUID.randomUUID()}.$ext"
        Files.copy(file.inputStream, dir.resolve(filename))
        return "temp/$filename"
    }

    /**
     * Перемещает обложку из temp/ в постоянное место events/{id}/cover.{ext}.
     * Вызывается внутри транзакции создания мероприятия.
     */
    override fun promoteTempCoverImage(
        tempPath: String,
        event: Event,
    ): String {
        val source: Path = Paths.get(uploadPath, tempPath)
        val ext = tempPath.substringAfterLast('.', "jpg")

        val targetDir: Path = Paths.get(uploadPath, "events", event.id.toString())
        Files.createDirectories(targetDir)

        val targetFilename = "cover.$ext"
        val target: Path = targetDir.resolve(targetFilename)

        Files.deleteIfExists(target)
        Files.move(source, target)

        return "events/${event.id}/$targetFilename"
    }

    /**
     * Удаляет файл с диска без выброса исключений.
     * Используется для отката при ошибке записи в БД.
     */
    override fun deleteFileQuietly(relativePath: String) {
        try {
            Files.deleteIfExists(Paths.get(uploadPath, relativePath))
        } catch (e: Exception) {
            log.warn("Failed to delete file $relativePath: ${e.message}")
        }
    }

    @Transactional
    @PreAuthorize("hasPermission(#photo, 'DELETE')")
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
