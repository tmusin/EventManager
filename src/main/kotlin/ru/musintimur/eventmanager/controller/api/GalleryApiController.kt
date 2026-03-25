package ru.musintimur.eventmanager.controller.api

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
    @PostMapping("/events/{eventId}")
    fun uploadPhoto(
        @PathVariable eventId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("caption", required = false) caption: String?,
        @AuthenticationPrincipal principal: UserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val event = eventService.findById(eventId)
        val uploader = userService.findByUsername(principal.username)
        val photo = galleryService.upload(event, uploader, file, caption)

        // Выдаём ACL загрузившему пользователю
        aclService.grantOwnerPermissions(photo, principal.username)

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
        galleryService.delete(photo)
        aclService.deleteAcl(photo)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
