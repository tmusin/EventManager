package ru.musintimur.eventmanager.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.musintimur.eventmanager.domain.Role
import ru.musintimur.eventmanager.service.UserService

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val userService: UserService,
) {
    @GetMapping("/users")
    fun userList(model: Model): String {
        model.addAttribute("users", userService.findAll())
        model.addAttribute("allRoles", Role.entries)
        return "admin/users"
    }

    @PostMapping("/users/{id}/roles")
    fun updateRoles(
        @PathVariable id: Long,
        @RequestParam roles: Set<String>,
    ): String {
        val roleSet = roles.map { Role.valueOf(it) }.toSet()
        userService.setRoles(id, roleSet)
        return "redirect:/admin/users"
    }

    @PostMapping("/users/{id}/toggle")
    fun toggleEnabled(
        @PathVariable id: Long,
    ): String {
        val user = userService.findById(id)
        userService.setEnabled(id, !user.enabled)
        return "redirect:/admin/users"
    }
}
