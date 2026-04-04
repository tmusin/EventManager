package ru.musintimur.eventmanager

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
abstract class AbstractIntegrationTest {
    companion object {
        // Контейнер стартует один раз для всей тест-сессии JVM
        // и останавливается автоматически при её завершении.
        // Порт назначается один раз и не меняется между тестами.
        private val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16-alpine")
                .withDatabaseName("eventmanager_test")
                .withUsername("test")
                .withPassword("test")
                .also { it.start() }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
