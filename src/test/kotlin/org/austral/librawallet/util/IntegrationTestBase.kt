package org.austral.librawallet.util

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class IntegrationTestBase {

    companion object {
        @Container
        val postgresqlContainer = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("libra_wallet_test")
            withUsername("testuser")
            withPassword("testpass")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresqlContainer::getUsername)
            registry.add("spring.datasource.password", postgresqlContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" } // Ensures schema is created and dropped for tests
        }
    }
} 
