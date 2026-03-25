package ru.musintimur.eventmanager.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "comments")
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    var author: User,
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,
    /**
     * true — автор является организатором мероприятия,
     * false — автор является участником (или просто зрителем).
     * Вычисляется и сохраняется в момент создания комментария.
     */
    @Column(name = "is_organizer", nullable = false)
    var isOrganizer: Boolean = false,
    /**
     * true — автор зарегистрирован на мероприятие как участник.
     */
    @Column(name = "is_participant", nullable = false)
    var isParticipant: Boolean = false,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
