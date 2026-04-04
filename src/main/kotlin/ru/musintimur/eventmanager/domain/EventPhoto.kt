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
@Table(name = "event_photos")
class EventPhoto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    var uploadedBy: User,
    /** Относительный путь к файлу внутри UPLOAD_PATH */
    @Column(name = "file_path", nullable = false)
    var filePath: String,
    @Column(length = 255)
    var caption: String? = null,
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
)
