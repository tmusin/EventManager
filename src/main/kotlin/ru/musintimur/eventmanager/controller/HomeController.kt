package ru.musintimur.eventmanager.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.musintimur.eventmanager.service.EventService

@Controller
class HomeController(
    private val eventService: EventService,
) {
    @GetMapping("/")
    fun index(
        @RequestParam(defaultValue = "0") page: Int,
        model: Model,
    ): String {
        val eventsPage = eventService.getApprovedPage(page)
        model.addAttribute("eventsPage", eventsPage)
        model.addAttribute("currentPage", page)
        return "index"
    }

    @GetMapping("/403")
    fun accessDenied(): String = "error/403"
}
