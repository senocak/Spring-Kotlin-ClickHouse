package com.github.senocak.util

import com.github.senocak.SpringKotlinApplication
import com.github.senocak.domain.User
import com.github.senocak.domain.dto.UserResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Properties

/**
 * @return -- UserResponse object
 */
fun User.convertEntityToDto(): UserResponse =
    UserResponse(
        name = this.name!!,
        email = this.email!!,
        roles = this.roles.map { "${it.name}" },
    )

fun <R : Any> R.logger(): Lazy<Logger> = lazy {
    LoggerFactory.getLogger((if (javaClass.kotlin.isCompanion) javaClass.enclosingClass else javaClass).name)
}

fun Int.randomStringGenerator(): String = RandomStringGenerator(length = this).next()

fun loadBuildInfo(): Properties =
    Properties()
        .also { p: Properties ->
            p.load(SpringKotlinApplication::class.java.getResourceAsStream("/build-info.properties"))
        }

fun String.fromProperties(): String = loadBuildInfo().getProperty(this)