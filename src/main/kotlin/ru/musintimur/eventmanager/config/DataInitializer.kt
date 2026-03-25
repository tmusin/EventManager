package ru.musintimur.eventmanager.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import ru.musintimur.eventmanager.domain.Role
import ru.musintimur.eventmanager.service.UserService

@Component
class DataInitializer(
    private val userService: UserService,
    @Value("\${app.admin.default-password}") private val adminDefaultPassword: String,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        userService.createIfNotExists(
            username = "admin",
            rawPassword = adminDefaultPassword,
            roles = setOf(Role.ADMIN),
        )
    }
}
