package me.gubin.simple.service.services

import me.gubin.simple.service.persistence.domains.Role
import me.gubin.simple.service.persistence.domains.RoleDomain
import me.gubin.simple.service.persistence.Roles
import me.gubin.simple.service.persistence.domains.toModel
import org.jetbrains.exposed.sql.transactions.transaction

class RoleService {
    fun findByName(name: String): Role {
        return transaction {
            RoleDomain.find { Roles.name eq name }.first().toModel()
        }
    }
}