package com.github.senocak.skch.config

import com.clickhouse.client.api.Client
import com.clickhouse.jdbc.ClickHouseDataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.Properties
import javax.sql.DataSource

@Component
@ConfigurationProperties(prefix = "spring.datasource")
class DataSourceConfig{
    lateinit var host: String
    lateinit var port: String
    lateinit var username: String
    lateinit var password: String
    lateinit var db: String
    lateinit var ddl: String

    @Bean
    fun chDirectClient(): Client =
        Client.Builder()
            .addEndpoint("http://$host:$port")
            .setUsername(username)
            .setPassword(password)
            .useNewImplementation(true) // using new transport layer implementation
            // sets the maximum number of connections to the server at a time
            // this is important for services handling many concurrent requests to ClickHouse
            .setMaxConnections(100)
            .setLZ4UncompressedBufferSize(1058576)
            .setSocketRcvbuf(500000)
            .setSocketTcpNodelay(true)
            .setSocketSndbuf(500000)
            .setClientNetworkBufferSize(500000)
            .allowBinaryReaderToReuseBuffers(true) // using buffer pool for binary reader
            .build()

    @Bean
    fun hikariDataSource(): DataSource {
        // connection pooling won't help much in terms of performance,
        // because the underlying implementation has its own pool.
        // for example: HttpURLConnection has a pool for sockets
        val poolConfig = HikariConfig()
        poolConfig.connectionTimeout = 5000L
        poolConfig.maximumPoolSize = 20
        poolConfig.maxLifetime = 300000L
        val properties: Properties = Properties()
            .also { it: Properties ->
                it.setProperty("custom_settings", "session_check=0,max_query_size=3000")
                it.setProperty("user", username)
                it.setProperty("password", password)
            }
        poolConfig.dataSource = ClickHouseDataSource("jdbc:clickhouse://$host:$port/$db?user=$username&password=$password", properties)
        //val connection = DriverManager.getConnection("jdbc:clickhouse://$host:$port/default?user=$username&password=$password", properties)
        //val connection = ClickHouseDriver().connect("jdbc:clickhouse://$host:$port/default?user=$username&password=$password", properties)
        return HikariDataSource(poolConfig)
    }
}