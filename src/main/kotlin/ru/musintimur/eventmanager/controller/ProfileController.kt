package ru.musintimur.eventmanager.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.musintimur.eventmanager.service.EventService
import ru.musintimur.eventmanager.service.RegistrationService
import ru.musintimur.eventmanager.service.UserService

data class ChangePasswordFormDto(
    @field:NotBlank(message = "Введите текущий пароль")
    val currentPassword: String = "",
    @field:NotBlank(message = "Введите новый пароль")
    @field:Size(min = 6, message = "Пароль от 6 символов")
    val newPassword: String = "",
    @field:NotBlank(message = "Подтвердите новый пароль")
    val newPasswordConfirm: String = "",
)

@Controller
@RequestMapping("/profile")
class ProfileController(
    private val userService: UserService,
    private val eventService: EventService,
    private val registrationService: RegistrationService,
) {
    @GetMapping
    fun profile(
        @AuthenticationPrincipal principal: UserDetails,
        model: Model,
    ): String {
        val user = userService.findByUsername(principal.username)
        val organizedEvents = eventService.getOrganizerEvents(user.id)
        val registrations = registrationService.getByUser(user.id)

        model.addAttribute("user", user)
        model.addAttribute("organizedEvents", organizedEvents)
        model.addAttribute("registrations", registrations)
        model.addAttribute("passwordForm", ChangePasswordFormDto())
        return "profile/index"
    }

    @PostMapping("/password")
    fun changePassword(
        @Valid @ModelAttribute("passwordForm") form: ChangePasswordFormDto,
        bindingResult: BindingResult,
        @AuthenticationPrincipal principal: UserDetails,
        model: Model,
    ): String {
        if (form.newPassword != form.newPasswordConfirm) {
            bindingResult.rejectValue("newPasswordConfirm", "error.form", "Пароли не совпадают")
        }
        if (!userService.checkPassword(principal.username, form.currentPassword)) {
            bindingResult.rejectValue("currentPassword", "error.form", "Неверный текущий пароль")
        }
        if (bindingResult.hasErrors()) {
            val user = userService.findByUsername(principal.username)
            model.addAttribute("user", user)
            model.addAttribute("organizedEvents", eventService.getOrganizerEvents(user.id))
            model.addAttribute("registrations", registrationService.getByUser(user.id))
            return "profile/index"
        }
        val user = userService.findByUsername(principal.username)
        userService.changePassword(user.id, form.newPassword)
        return "redirect:/profile?passwordChanged"
    }
}
