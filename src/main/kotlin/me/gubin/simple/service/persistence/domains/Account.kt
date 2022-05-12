package me.gubin.simple.service.persistence.domains

import io.ktor.server.auth.*
import me.gubin.simple.service.persistence.Accounts
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*


class AccountDomain(uuid: EntityID<UUID>) : UUIDEntity(uuid) {
    companion object : EntityClass<UUID, AccountDomain>(Accounts)

    var username by Accounts.username
    var password by Accounts.password
    var role by RoleDomain referencedOn Accounts.role
}

open class Account(
    open val uuid: UUID,
    open val username: String,
    open val password: String,
    open val role: Role,
) : Principal

fun AccountDomain.toModel(erasePassword: Boolean = true): Account {
    return Account(
        this.id.value,
        this.username,
        if (erasePassword) "" else this.password,
        this.role.toModel()
    )
}