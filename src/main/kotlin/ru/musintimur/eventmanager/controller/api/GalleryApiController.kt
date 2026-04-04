package ru.musintimur.eventmanager.controller.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.musintimur.eventmanager.domain.EventStatus
import ru.musintimur.eventmanager.service.AclService
import ru.musintimur.eventmanager.service.EventService
import ru.musintimur.eventmanager.service.GalleryService
import ru.musintimur.eventmanager.service.UserService

@RestController
@RequestMapping("/api/gallery")
class GalleryApiController(
    private val galleryService: GalleryService,
    private val eventService: EventService,
    private val userService: UserService,
    private val aclService: AclService,
) {
    private val log: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    @PostMapping("/events/{eventId}")
    fun uploadPhoto(
        @PathVariable eventId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("caption", required = false) caption: String?,
        @AuthenticationPrincipal principal: UserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val event = eventService.findById(eventId)
        require(event.status == EventStatus.COMPLETED) {
            "Фотографии можно добавлять только после завершения мероприятия"
        }
        val uploader = userService.findByUsername(principal.username)

        // Шаг 1: сохраняем файл на диск (вне транзакции)
        val filePath = galleryService.savePhotoFile(event, file)

        // Шаг 2: сохраняем запись в БД + выдаём ACL.
        // При любой ошибке — удаляем файл с диска.
        val photo =
            try {
                val savedPhoto = galleryService.savePhotoRecord(event, uploader, filePath, caption)
                // ACL выдаём после успешной записи в БД, в отдельной транзакции.
                // Если ACL упадёт — файл и запись в БД останутся, но без прав.
                // Обрабатываем это отдельным catch.
                aclService.grantOwnerPermissions(savedPhoto, principal.username)
                savedPhoto
            } catch (dbEx: Exception) {
                log.error(
                    "Ошибка сохранения фото в БД или ACL для события #$eventId, удаляем файл: $filePath",
                    dbEx,
                )
                galleryService.deleteFileQuietly(filePath)
                throw dbEx
            }

        return ResponseEntity.ok(
            mapOf(
                "id" to photo.id,
                "filePath" to photo.filePath,
                "caption" to (photo.caption ?: ""),
                "uploadedBy" to photo.uploadedBy.username,
            ),
        )
    }

    @DeleteMapping("/{photoId}")
    fun deletePhoto(
        @PathVariable photoId: Long,
    ): ResponseEntity<Map<String, Any>> {
        val photo = galleryService.findById(photoId)
        // Сначала удаляем ACL, затем файл и запись в БД.
        // Порядок важен: если deleteAcl упадёт — файл и запись остаются целыми.
        aclService.deleteAcl(photo)
        galleryService.delete(photo)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
