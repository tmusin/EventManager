package ru.musintimur.eventmanager.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.AbstractIntegrationTest
import ru.musintimur.eventmanager.domain.Role
import ru.musintimur.eventmanager.domain.User

@Transactional
class UserRepositoryTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `findByUsername returns user when exists`() {
        val user =
            userRepository.save(
                User(username = "alice", password = "hashed"),
            )

        val found = userRepository.findByUsername("alice")

        assertTrue(found.isPresent)
        assertEquals(user.id, found.get().id)
    }

    @Test
    fun `findByUsername returns empty when not exists`() {
        val found = userRepository.findByUsername("nobody")
        assertFalse(found.isPresent)
    }

    @Test
    fun `existsByUsername returns true for existing user`() {
        userRepository.save(User(username = "bob", password = "hashed"))
        assertTrue(userRepository.existsByUsername("bob"))
    }

    @Test
    fun `existsByUsername returns false for missing user`() {
        assertFalse(userRepository.existsByUsername("ghost"))
    }

    @Test
    fun `user is saved with default USER role`() {
        val user = userRepository.save(User(username = "charlie", password = "hashed"))
        val found = userRepository.findByUsername("charlie").get()
        assertTrue(found.roles.contains(Role.USER))
    }
}
