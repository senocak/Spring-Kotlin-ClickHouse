package com.github.senocak.skch.service

import com.github.senocak.skch.config.DataSourceConfig
import com.github.senocak.skch.domain.Role
import com.github.senocak.skch.domain.User
import com.github.senocak.skch.util.RoleName
import com.github.senocak.skch.util.logger
import org.slf4j.Logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Profiles
import org.springframework.scheduling.annotation.Async
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
@Async
class Listeners(
    private val dataSourceConfig: DataSourceConfig,
    private val userService: UserService,
    private val roleService: RoleService,
    private val passwordEncoder: PasswordEncoder
){
    private val log: Logger by logger()

    // FIXME: @Profile("!integration-test")
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent(event: ApplicationReadyEvent) {
        if (event.applicationContext.environment.acceptsProfiles(Profiles.of("integration-test")))
            return
        if (dataSourceConfig.ddl == "populate") {
            roleService.deleteAll()
            userService.deleteAllUsers()
            val userRole: Role = roleService.save(roleName = RoleName.ROLE_USER)
            val adminRole: Role = roleService.save(roleName = RoleName.ROLE_ADMIN)
            User(name = "anil1", email = "anil1@senocak.com", password = passwordEncoder.encode("asenocak"))
                .also {
                    it.roles = listOf(userRole, adminRole)
                }
                .run {
                    userService.save(user = this)
                }

            User(name = "anil2", email = "anil2@gmail.com", password = passwordEncoder.encode("asenocak"))
                .also {
                    it.roles = listOf(element = userRole)
                }
                .run {
                    userService.save(user = this)
                }

            User(name = "anil3", email = "anil3@gmail.com", password = passwordEncoder.encode("asenocak"))
                .also {
                    it.roles = listOf(element = userRole)
                }
                .run {
                    userService.save(user = this)
                }
            log.info("Seeding completed")
        }
    }
}
