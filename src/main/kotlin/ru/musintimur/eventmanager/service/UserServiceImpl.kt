package ru.musintimur.eventmanager.service

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.musintimur.eventmanager.domain.Role
import ru.musintimur.eventmanager.domain.User
import ru.musintimur.eventmanager.repository.UserRepository

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository
                .findByUsername(username)
                .orElseThrow { UsernameNotFoundException("Пользователь не найден: $username") }
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            user.enabled,
            true,
            true,
            true,
            user.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") },
        )
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): User =
        userRepository
            .findById(id)
            .orElseThrow { NoSuchElementException("Пользователь #$id не найден") }

    @Transactional(readOnly = true)
    override fun findByUsername(username: String): User =
        userRepository
            .findByUsername(username)
            .orElseThrow { NoSuchElementException("Пользователь '$username' не найден") }

    @Transactional(readOnly = true)
    override fun findAll(): List<User> = userRepository.findAll()

    @Transactional(readOnly = true)
    override fun existsByUsername(username: String): Boolean = userRepository.existsByUsername(username)

    /** Регистрация нового пользователя с ролью USER */
    @Transactional
    override fun register(
        username: String,
        rawPassword: String,
    ): User {
        require(!userRepository.existsByUsername(username)) {
            "Пользователь '$username' уже существует"
        }
        val user =
            User(
                username = username,
                password = passwordEncoder.encode(rawPassword),
            )
        return userRepository.save(user)
    }

    /** Смена пароля из личного кабинета */
    @Transactional
    override fun changePassword(
        userId: Long,
        rawPassword: String,
    ) {
        val user = findById(userId)
        user.password = passwordEncoder.encode(rawPassword)
        userRepository.save(user)
    }

    /** Проверка текущего пароля перед сменой */
    override fun checkPassword(
        username: String,
        rawPassword: String,
    ): Boolean {
        val user =
            userRepository
                .findByUsername(username)
                .orElseThrow { UsernameNotFoundException("Пользователь не найден: $username") }
        return passwordEncoder.matches(rawPassword, user.password)
    }

    /** Назначение / снятие роли администратором */
    @Transactional
    override fun setRoles(
        userId: Long,
        roles: Set<Role>,
    ) {
        val user = findById(userId)
        user.roles = roles.toMutableSet()
        userRepository.save(user)
    }

    /** Блокировка / разблокировка пользователя */
    @Transactional
    override fun setEnabled(
        userId: Long,
        enabled: Boolean,
    ) {
        val user = findById(userId)
        user.enabled = enabled
        userRepository.save(user)
    }

    /** Создание пользователя с заданной ролью (используется в DataInitializer) */
    @Transactional
    override fun createIfNotExists(
        username: String,
        rawPassword: String,
        roles: Set<Role>,
    ): User =
        userRepository.findByUsername(username).orElseGet {
            userRepository.save(
                User(
                    username = username,
                    password = passwordEncoder.encode(rawPassword),
                    roles = roles.toMutableSet(),
                ),
            )
        }
}
