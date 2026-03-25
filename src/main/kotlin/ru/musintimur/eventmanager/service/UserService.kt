package ru.musintimur.eventmanager.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import ru.musintimur.eventmanager.domain.Role
import ru.musintimur.eventmanager.domain.User

interface UserService : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails

    fun findById(id: Long): User

    fun findByUsername(username: String): User

    fun findAll(): List<User>

    fun existsByUsername(username: String): Boolean

    fun register(
        username: String,
        rawPassword: String,
    ): User

    fun changePassword(
        userId: Long,
        rawPassword: String,
    )

    fun checkPassword(
        username: String,
        rawPassword: String,
    ): Boolean

    fun setRoles(
        userId: Long,
        roles: Set<Role>,
    )

    fun setEnabled(
        userId: Long,
        enabled: Boolean,
    )

    fun createIfNotExists(
        username: String,
        rawPassword: String,
        roles: Set<Role>,
    ): User
}
