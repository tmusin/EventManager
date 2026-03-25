package ru.musintimur.eventmanager.controller.api

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.musintimur.eventmanager.service.EventService
import ru.musintimur.eventmanager.service.RegistrationService
import ru.musintimur.eventmanager.service.UserService

@RestController
@RequestMapping("/api/registrations")
class RegistrationApiController(
    private val registrationService: RegistrationService,
    private val eventService: EventService,
    private val userService: UserService,
) {
    /** Записаться на мероприятие */
    @PostMapping("/events/{eventId}")
    fun register(
        @PathVariable eventId: Long,
        @AuthenticationPrincipal principal: UserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val event = eventService.getApprovedById(eventId)
        val user = userService.findByUsername(principal.username)
        registrationService.register(event, user)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Вы записаны на мероприятие"))
    }

    /** Отменить свою запись */
    @DeleteMapping("/events/{eventId}")
    fun cancel(
        @PathVariable eventId: Long,
        @AuthenticationPrincipal principal: UserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val user = userService.findByUsername(principal.username)
        registrationService.cancel(eventId, user.id)
        return ResponseEntity.ok(mapOf("success" to true, "message" to "Запись отменена"))
    }

    /** Организатор: отклонить участника */
    @PostMapping("/{registrationId}/reject")
    fun rejectParticipant(
        @PathVariable registrationId: Long,
    ): ResponseEntity<Map<String, Any>> {
        registrationService.rejectParticipant(registrationId)
        return ResponseEntity.ok(mapOf("success" to true))
    }

    /** Организатор: отметить участника как оплатившего */
    @PostMapping("/{registrationId}/paid")
    fun markAsPaid(
        @PathVariable registrationId: Long,
    ): ResponseEntity<Map<String, Any>> {
        registrationService.markAsPaid(registrationId)
        return ResponseEntity.ok(mapOf("success" to true))
    }
}
