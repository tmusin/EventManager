package ru.musintimur.eventmanager.service

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.domain.Comment
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.CommentRepository
import ru.musintimur.eventmanager.repository.EventRegistrationRepository

@Service
class CommentServiceImpl(
    private val commentRepository: CommentRepository,
    private val registrationRepository: EventRegistrationRepository,
) : CommentService {
    @Transactional(readOnly = true)
    override fun findById(id: Long): Comment =
        commentRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Комментарий #$id не найден") }

    @Transactional(readOnly = true)
    override fun getByEvent(eventId: Long): List<Comment> = commentRepository.findAllByEventIdOrderByCreatedAtAsc(eventId)

    @Transactional
    override fun addComment(
        event: Event,
        author: User,
        content: String,
    ): Comment {
        val isOrganizer = event.organizer.id == author.id
        val isParticipant = registrationRepository.existsByEventIdAndUserId(event.id, author.id)
        val comment =
            Comment(
                event = event,
                author = author,
                content = content,
                isOrganizer = isOrganizer,
                isParticipant = isParticipant,
            )
        return commentRepository.save(comment)
    }

    @Transactional
    @PreAuthorize("hasPermission(#comment, 'DELETE')")
    override fun delete(comment: Comment) {
        commentRepository.delete(comment)
    }
}
