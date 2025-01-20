package com.github.senocak.skch.domain

import com.github.senocak.skch.util.RoleName
import org.springframework.data.repository.CrudRepository

interface RoleRepository: CrudRepository<Role, String> {
    fun findByName(roleName: RoleName): Role?
}
interface UserRepository: CrudRepository<User, String> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean
}