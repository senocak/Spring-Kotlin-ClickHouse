package com.github.senocak.skch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringKotlinClickHouseApplication

fun main(args: Array<String>) {
    runApplication<SpringKotlinClickHouseApplication>(*args)
}
