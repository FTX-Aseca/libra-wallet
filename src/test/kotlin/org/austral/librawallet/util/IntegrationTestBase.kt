package org.austral.librawallet.util

import org.slf4j.LoggerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

@Testcontainers
abstract class IntegrationTestBase {

    companion object {
        private val logger = LoggerFactory.getLogger(IntegrationTestBase::class.java)

        @Container
        val postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("libra_wallet_test")
            withUsername("testuser")
            withPassword("testpass")
            withLogConsumer(Slf4jLogConsumer(logger))
            withStartupTimeout(Duration.ofSeconds(120))
            setWaitStrategy(
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(90)),
            )
            withCommand("postgres", "-c", "log_statement=all")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresqlContainer::getUsername)
            registry.add("spring.datasource.password", postgresqlContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.datasource.hikari.initializationFailTimeout") { "120000" }
            registry.add("spring.datasource.hikari.connectionTimeout") { "10000" }
        }
    }
}
