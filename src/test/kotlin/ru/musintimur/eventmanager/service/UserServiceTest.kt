package ru.musintimur.eventmanager.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.AbstractIntegrationTest
import ru.musintimur.eventmanager.domain.Role

@Transactional
class UserServiceTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `register creates user with encoded password`() {
        val user = userService.register("testuser_${System.nanoTime()}", "password123")

        assertNotNull(user.id)
        assertNotEquals("password123", user.password) // пароль должен быть захеширован
        assertTrue(user.roles.contains(Role.USER))
        assertTrue(user.enabled)
    }

    @Test
    fun `register throws when username already taken`() {
        val username = "duplicate_${System.nanoTime()}"
        userService.register(username, "pass1")

        assertThrows(IllegalArgumentException::class.java) {
            userService.register(username, "pass2")
        }
    }

    @Test
    fun `checkPassword returns true for correct password`() {
        val username = "passcheck_${System.nanoTime()}"
        userService.register(username, "secret")

        assertTrue(userService.checkPassword(username, "secret"))
    }

    @Test
    fun `checkPassword returns false for wrong password`() {
        val username = "passcheck2_${System.nanoTime()}"
        userService.register(username, "secret")

        assertFalse(userService.checkPassword(username, "wrong"))
    }

    @Test
    fun `setEnabled disables user`() {
        val user = userService.register("disabletest_${System.nanoTime()}", "pass")
        userService.setEnabled(user.id, false)

        val updated = userService.findById(user.id)
        assertFalse(updated.enabled)
    }

    @Test
    fun `setRoles updates user roles`() {
        val user = userService.register("roletest_${System.nanoTime()}", "pass")
        userService.setRoles(user.id, setOf(Role.USER, Role.MANAGER))

        val updated = userService.findById(user.id)
        assertTrue(updated.roles.contains(Role.MANAGER))
    }

    @Test
    fun `createIfNotExists does not duplicate user`() {
        val username = "idempotent_${System.nanoTime()}"
        userService.createIfNotExists(username, "pass", setOf(Role.ADMIN))
        userService.createIfNotExists(username, "pass2", setOf(Role.USER))

        val user = userService.findByUsername(username)
        // Роль должна остаться ADMIN — второй вызов не перезаписывает
        assertTrue(user.roles.contains(Role.ADMIN))
        assertEquals(1, userService.findAll().count { it.username == username })
    }
}
