package ru.musintimur.eventmanager.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "events")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, length = 200)
    var title: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,
    /** Относительный путь к файлу обложки внутри UPLOAD_PATH */
    @Column(name = "cover_image_path")
    var coverImagePath: String? = null,
    @Column(name = "event_date", nullable = false)
    var eventDate: LocalDateTime,
    /** Стоимость участия. 0 — бесплатно */
    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,
    /**
     * Максимальное число участников.
     * 0 — неограниченное количество.
     */
    @Column(name = "max_participants", nullable = false)
    var maxParticipants: Int = 0,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: EventStatus = EventStatus.PENDING,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id", nullable = false)
    var organizer: User,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
