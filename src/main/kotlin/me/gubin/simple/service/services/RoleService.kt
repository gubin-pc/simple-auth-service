package me.gubin.simple.service.services

import me.gubin.simple.service.persistence.Roles
import me.gubin.simple.service.persistence.domains.Role
import me.gubin.simple.service.persistence.domains.RoleDomain
import me.gubin.simple.service.persistence.domains.RoleName
import me.gubin.simple.service.persistence.domains.toModel
import org.jetbrains.exposed.sql.transactions.transaction

class RoleService {
    fun findByName(roleName: RoleName): Role {
        return transaction {
            RoleDomain.find { Roles.name eq roleName.name }.first().toModel()
        }
    }

    fun findAll(): List<Role> {
        return transaction {
            RoleDomain.all().map { it.toModel() }
        }
    }
}