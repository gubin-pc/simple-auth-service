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
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class AccountService {

    fun create(username: String, password: String, role: String): Account = transaction {
        AccountDomain.new(UUID.randomUUID()) {
            this.username = username
            this.password = digestFunction(password).encodeBase64()
            this.role = RoleDomain.find { Roles.name eq role }.firstOrNull() ?: throw IllegalArgumentException()
        }.toModel()
    }

    fun update(username: String, newPassword: String): Account = transaction {
        val account = AccountDomain.find { Accounts.username eq username }.first()
        account.password = digestFunction(newPassword).encodeBase64()
        account.flush()
        account.toModel()
    }

    fun findBy(username: String, erasePassword: Boolean = true): Account? = transaction {
            AccountDomain.find { Accounts.username eq username }
                .with(AccountDomain::role)
                .firstOrNull()
                ?.toModel(erasePassword)
    }

}

