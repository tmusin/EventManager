package ru.musintimur.eventmanager.service

import org.springframework.web.multipart.MultipartFile
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.EventPhoto
import ru.musintimur.eventmanager.domain.User

interface GalleryService {
    fun findById(id: Long): EventPhoto

    fun getByEvent(eventId: Long): List<EventPhoto>

    /** Сохраняет файл на диск, возвращает относительный путь. Вне транзакции. */
    fun savePhotoFile(
        event: Event,
        file: MultipartFile,
    ): String

    /** Сохраняет запись о фото в БД. Транзакционно. */
    fun savePhotoRecord(
        event: Event,
        uploader: User,
        filePath: String,
        caption: String?,
    ): EventPhoto

    /**
     * Сохраняет обложку во временную папку до создания мероприятия в БД.
     * Возвращает временный относительный путь.
     * После успешного создания мероприятия файл перемещается в постоянное место
     * внутри [EventService.create].
     */
    fun saveTempCoverImage(file: MultipartFile): String

    /**
     * Перемещает временный файл обложки в постоянное место после получения id мероприятия.
     * Возвращает постоянный относительный путь.
     */
    fun promoteTempCoverImage(
        tempPath: String,
        event: Event,
    ): String

    /** Тихо удаляет файл с диска. Используется для отката при ошибке БД. */
    fun deleteFileQuietly(relativePath: String)

    fun delete(photo: EventPhoto)

    fun saveCoverImage(
        event: Event,
        file: MultipartFile,
    ): String
}
