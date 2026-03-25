package ru.musintimur.eventmanager.domain

enum class RegistrationStatus {
    /** Ожидает подтверждения организатором (если потребуется) */
    PENDING,

    /** Участие подтверждено */
    CONFIRMED,

    /** Участник оплатил участие */
    PAID,

    /** Отклонён организатором */
    REJECTED,
}
