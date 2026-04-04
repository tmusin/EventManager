package ru.musintimur.eventmanager.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.musintimur.eventmanager.domain.User
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): Optional<User>

    fun existsByUsername(username: String): Boolean
}
