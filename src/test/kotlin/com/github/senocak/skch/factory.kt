package com.github.senocak.skch

import com.github.senocak.skch.TestConstants.USER_EMAIL
import com.github.senocak.skch.TestConstants.USER_NAME
import com.github.senocak.skch.TestConstants.USER_PASSWORD
import com.github.senocak.skch.domain.Role
import com.github.senocak.skch.domain.User
import com.github.senocak.skch.util.RoleName
import java.util.ArrayList
import java.util.UUID

fun createTestUser(): User =
    User(name = USER_NAME, email = USER_EMAIL, password = USER_PASSWORD)
        .also { it: User ->
            it.id = UUID.randomUUID().toString()
            it.roles = arrayListOf<Role>()
                .also { list: ArrayList<Role> -> list.add(element = createRole(roleName = RoleName.ROLE_USER)) }
                .also { list: ArrayList<Role> -> list.add(element = createRole(roleName = RoleName.ROLE_ADMIN)) }
        }

fun createRole(roleName: RoleName? = RoleName.ROLE_USER): Role =
    Role(name = roleName)
        .also { it.id = UUID.randomUUID().toString() }
