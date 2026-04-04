package ru.musintimur.eventmanager.domain

enum class EventStatus {
    /** Создано организатором, ожидает проверки менеджером */
    PENDING,

    /** Проверено менеджером, отображается на главной */
    APPROVED,

    /** Отклонено менеджером */
    REJECTED,

    /** Мероприятие состоялось (устанавливает организатор) */
    COMPLETED,
}
