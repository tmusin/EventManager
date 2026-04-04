package ru.musintimur.eventmanager.controller.api

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.musintimur.eventmanager.service.AclService
import ru.musintimur.eventmanager.service.CommentService
import ru.musintimur.eventmanager.service.EventService
import ru.musintimur.eventmanager.service.UserService
import java.time.format.DateTimeFormatter

data class AddCommentRequestDto(
    val content: String,
)

@RestController
@RequestMapping("/api/comments")
class CommentApiController(
    private val commentService: CommentService,
    private val eventService: EventService,
    private val userService: UserService,
    private val aclService: AclService,
) {
    @PostMapping("/events/{eventId}")
    fun addComment(
        @PathVariable eventId: Long,
        @RequestBody request: AddCommentRequestDto,
        @AuthenticationPrincipal principal: UserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val event = eventService.findById(eventId)
        val author = userService.findByUsername(principal.username)
        val comment = commentService.addComment(event, author, request.content)

        // Выдаём ACL автору комментария
        aclService.grantOwnerPermissions(comment, principal.username)

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        return ResponseEntity.ok(
            mapOf(
                "id" to comment.id,
                "content" to comment.content,
                "author" to comment.author.username,
                "isOrganizer" to comment.isOrganizer,
                "isParticipant" to comment.isParticipant,
                "createdAt" to comment.createdAt.format(formatter),
            ),
        )
    }

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
    ): ResponseEntity<Map<String, Any>> {
        val comment = commentService.findById(commentId)
        commentService.delete(comment)
        aclService.deleteAcl(comment)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
