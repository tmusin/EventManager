package ru.musintimur.eventmanager.service

import ru.musintimur.eventmanager.domain.Comment
import ru.musintimur.eventmanager.domain.Event
import ru.musintimur.eventmanager.domain.User

interface CommentService {
    fun findById(id: Long): Comment

    fun getByEvent(eventId: Long): List<Comment>

    fun addComment(
        event: Event,
        author: User,
        content: String,
    ): Comment

    fun delete(comment: Comment)
}
