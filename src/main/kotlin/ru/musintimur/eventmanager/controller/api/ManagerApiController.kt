package ru.musintimur.eventmanager.controller.api

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.musintimur.eventmanager.service.EventService

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
class ManagerApiController(
    private val eventService: EventService,
) {
    @PostMapping("/events/{id}/approve")
    fun approve(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Any>> {
        eventService.approve(id)
        return ResponseEntity.ok(mapOf("success" to true, "status" to "APPROVED"))
    }

    @PostMapping("/events/{id}/reject")
    fun reject(
        @PathVariable id: Long,
    ): ResponseEntity<Map<String, Any>> {
        eventService.reject(id)
        return ResponseEntity.ok(mapOf("success" to true, "status" to "REJECTED"))
    }
}
