package com.github.senocak.skch.security

import com.github.senocak.skch.domain.Role
import com.github.senocak.skch.domain.User
import com.github.senocak.skch.service.UserService
import com.github.senocak.skch.util.RoleName
import com.github.senocak.skch.util.logger
import org.slf4j.Logger
import org.slf4j.MDC
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationManager(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder
): AuthenticationManager {
    private val log: Logger by logger()

    override fun authenticate(authentication: Authentication): Authentication {
        val user: User = userService.findByEmail(email = authentication.name)
        if (authentication.credentials != null){
            val matches: Boolean = passwordEncoder.matches(authentication.credentials.toString(), user.password)
            if (!matches) {
                "Username or password invalid. AuthenticationCredentialsNotFoundException occurred for ${user.name}"
                    .apply { log.error(this) }
                    .run { throw AuthenticationCredentialsNotFoundException(this) }
            }
        }
        val authorities: MutableCollection<SimpleGrantedAuthority> = ArrayList()
        authorities.add(element = SimpleGrantedAuthority(RoleName.ROLE_USER.role))
        if (user.roles.stream().anyMatch { it: Role -> it.name!!.role == RoleName.ROLE_ADMIN.role })
            authorities.add(element = SimpleGrantedAuthority(RoleName.ROLE_ADMIN.role))
        val loadUserByUsername: org.springframework.security.core.userdetails.User = userService.loadUserByUsername(authentication.name)
        val auth: Authentication = UsernamePasswordAuthenticationToken(loadUserByUsername, user.password, authorities)
        SecurityContextHolder.getContext().authentication = auth
        MDC.put("userId", "${user.id}")
        log.info("Authentication is set to SecurityContext for ${user.name}")
        return auth
    }
}