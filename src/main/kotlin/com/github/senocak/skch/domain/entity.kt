package com.github.senocak.skch.domain

import com.github.senocak.skch.util.RoleName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.io.Serializable
import java.util.Date

@MappedSuperclass
open class BaseDomain(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    var id: String? = null,
    @Column var createdAt: Date = Date(),
    @Column var updatedAt: Date = Date()
) : Serializable

@Entity
@Table(name = "users", uniqueConstraints = [UniqueConstraint(columnNames = ["email"])])
data class User(
    @Column(name = "name", nullable = false, length = 50) var name: String? = null,
    @Column(name = "email", nullable = false, length = 100) var email: String? = null,
    @Column(name = "password", nullable = false) var password: String? = null
) : BaseDomain() {
    @JoinTable(name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id", nullable = false, foreignKey = ForeignKey(name = "fk_user_roles_user_id"))],
        inverseJoinColumns = [JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = ForeignKey(name = "fk_user_roles_role_id")
        )],
        uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "role_id"], name = "uk_user_roles_unique_user_id_role_id")]
    )
    @ManyToMany(fetch = FetchType.EAGER)
    var roles: List<Role> = arrayListOf()
}

@Entity
@Table(name = "roles")
data class Role(@Column @Enumerated(EnumType.STRING) var name: RoleName? = null): BaseDomain()
