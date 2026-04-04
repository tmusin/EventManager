package ru.musintimur.eventmanager.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.musintimur.eventmanager.service.EventService

@Controller
@RequestMapping("/manager")
class ManagerController(
    private val eventService: EventService,
) {
    @GetMapping
    fun pendingList(model: Model): String {
        model.addAttribute("events", eventService.getPendingEvents())
        return "manager/pending"
    }
}
