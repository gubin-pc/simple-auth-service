package me.gubin.simple.service.services

import io.ktor.util.*
import me.gubin.simple.service.persistence.domains.Account
import me.gubin.simple.service.persistence.domains.AccountDomain
import me.gubin.simple.service.persistence.Accounts
import me.gubin.simple.service.persistence.domains.RoleDomain
import me.gubin.simple.service.persistence.Roles
import me.gubin.simple.service.persistence.domains.toModel
import me.gubin.simple.service.digestFunction
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class AccountService {

    fun create(username: String, password: String, role: String): Account = transaction {
        AccountDomain.new(UUID.randomUUID()) {
            this.username = username
            this.password = digestFunction(password).encodeBase64()
            this.role = RoleDomain.find { Roles.name eq role }.firstOrNull() ?: throw IllegalArgumentException()
        }.toModel()
    }

    fun changePassword(username: String, currentPassword: String, newPassword: String): Boolean = transaction {

        if (findBy(username, false)?.password != digestFunction(currentPassword).encodeBase64()) {
            throw IllegalArgumentException("incorrect current password")
        }

        Accounts.update({
            (Accounts.username eq username)
                .and(Accounts.password eq digestFunction(currentPassword).encodeBase64())
        }) {
            it[password] = digestFunction(newPassword).encodeBase64()
        } != 0
    }

    fun findBy(username: String, erasePassword: Boolean = true): Account? = transaction {
            AccountDomain.find { Accounts.username eq username }
                .with(AccountDomain::role)
                .firstOrNull()
                ?.toModel(erasePassword)
    }

}

