package ru.musintimur.eventmanager.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    // Публичный доступ
                    .requestMatchers("/", "/events/{id}", "/register", "/login")
                    .permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**")
                    .permitAll()
                    // Только ADMIN
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    // Только MANAGER
                    .requestMatchers("/manager/**")
                    .hasRole("MANAGER")
                    // Всё остальное — только аутентифицированные
                    .anyRequest()
                    .authenticated()
            }.formLogin { form ->
                form
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error")
                    .permitAll()
            }.logout { logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .permitAll()
            }.exceptionHandling { ex ->
                ex.accessDeniedPage("/403")
            }

        return http.build()
    }
}
