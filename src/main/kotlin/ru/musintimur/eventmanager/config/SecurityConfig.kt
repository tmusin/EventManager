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
                    // Служебные пути мероприятий — только для аутентифицированных
                    .requestMatchers("/events/new", "/events/*/edit", "/events/*/complete")
                    .authenticated()
                    // Публичный доступ
                    .requestMatchers("/", "/events/*", "/register", "/login", "/403")
                    .permitAll()
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**")
                    .permitAll()
                    // Только ADMIN
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    // Менеджерские страницы — MANAGER или ADMIN
                    .requestMatchers("/manager/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    // Менеджерский API — MANAGER или ADMIN
                    .requestMatchers("/api/manager/**")
                    .hasAnyRole("MANAGER", "ADMIN")
                    // Остальные API — только аутентифицированные
                    .requestMatchers("/api/**")
                    .authenticated()
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
