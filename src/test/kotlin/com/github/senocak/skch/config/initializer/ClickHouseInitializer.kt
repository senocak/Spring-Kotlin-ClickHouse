package com.github.senocak.skch.config.initializer

import com.github.senocak.skch.TestConstants.CONTAINER_ATTEMPT
import com.github.senocak.skch.TestConstants.CONTAINER_WAIT_TIMEOUT
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.clickhouse.ClickHouseContainer

@TestConfiguration
class ClickHouseInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
        TestPropertyValues.of(
            "spring.datasource.jdbcUrl=${couchbaseContainer.jdbcUrl}",
            "spring.datasource.host=localhost",
            "spring.datasource.port=${couchbaseContainer.getMappedPort(EXPOSED_PORT)}",
            "spring.datasource.username=${couchbaseContainer.username}",
            "spring.datasource.password=${couchbaseContainer.password}",
            "spring.datasource.db=${couchbaseContainer.databaseName}",
            "spring.datasource.ddl=populate"
        ).applyTo(configurableApplicationContext.environment)
    }

    companion object {
        const val EXPOSED_PORT = 8123
        private val couchbaseContainer: ClickHouseContainer = ClickHouseContainer("clickhouse/clickhouse-server:24.3.6")
            .withExposedPorts(EXPOSED_PORT)
            .withDatabaseName("testcontainer")
            .withUsername("anil")
            .withPassword("senocak")
            .withStartupAttempts(CONTAINER_ATTEMPT)
            .withStartupTimeout(CONTAINER_WAIT_TIMEOUT)
            .withInitScripts("migrations/V1__create_tables.sql", "populate_db.sql")
        init {
            couchbaseContainer.start()
        }
    }
}