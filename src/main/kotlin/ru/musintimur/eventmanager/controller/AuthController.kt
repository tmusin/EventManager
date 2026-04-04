package ru.musintimur.eventmanager.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import ru.musintimur.eventmanager.service.UserService

data class RegisterFormDto(
    @field:NotBlank(message = "Логин не может быть пустым")
    @field:Size(min = 3, max = 64, message = "Логин от 3 до 64 символов")
    val username: String = "",
    @field:NotBlank(message = "Пароль не может быть пустым")
    @field:Size(min = 6, max = 255, message = "Пароль от 6 символов")
    val password: String = "",
    @field:NotBlank(message = "Подтвердите пароль")
    val passwordConfirm: String = "",
)

@Controller
class AuthController(
    private val userService: UserService,
) {
    @GetMapping("/login")
    fun loginPage(): String = "auth/login"

    @GetMapping("/register")
    fun registerPage(model: Model): String {
        model.addAttribute("form", RegisterFormDto())
        return "auth/register"
    }

    @PostMapping("/register")
    fun register(
        @Valid @ModelAttribute("form") form: RegisterFormDto,
        bindingResult: BindingResult,
    ): String {
        if (form.password != form.passwordConfirm) {
            bindingResult.rejectValue("passwordConfirm", "error.form", "Пароли не совпадают")
        }
        if (userService.existsByUsername(form.username)) {
            bindingResult.rejectValue("username", "error.form", "Логин уже занят")
        }
        if (bindingResult.hasErrors()) {
            return "auth/register"
        }
        userService.register(form.username, form.password)
        return "redirect:/login?registered"
    }
}
