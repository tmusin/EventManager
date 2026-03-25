package ru.musintimur.eventmanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.musintimur.eventmanager.domain.Comment

interface CommentRepository : JpaRepository<Comment, Long> {
    fun findAllByEventIdOrderByCreatedAtAsc(eventId: Long): List<Comment>

    fun findAllByAuthorId(authorId: Long): List<Comment>
}
