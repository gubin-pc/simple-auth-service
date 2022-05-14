package me.gubin.simple.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.core.spec.style.ShouldSpec
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.util.*
import me.gubin.simple.service.persistence.Roles
import me.gubin.simple.service.persistence.domains.Account
import me.gubin.simple.service.persistence.domains.AccountDomain
import me.gubin.simple.service.persistence.domains.RoleDomain
import me.gubin.simple.service.persistence.domains.toModel
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

open class IntegrationSpec : ShouldSpec() {

    val client: HttpClient = HttpClient(CIO) {
        defaultRequest {
            url("http://localhost:8080/")
        }
    }

    suspend fun withAccount(
        role: String = "Admin",
        username: String = Instant.now().toEpochMilli().toString(),
        password: String = Instant.now().toEpochMilli().toString(),
        block: suspend (username: String, password: String, role: String) -> Unit,
    ) {
        transaction {
            AccountDomain.new(UUID.randomUUID()) {
                this.username = username
                this.password = digestFunction(password).encodeBase64()
                this.role = RoleDomain.find { Roles.name eq role }.first()
            }.toModel()
        }
        block(username, password, role)
    }


    fun write(value: Any) = jacksonObjectMapper().writeValueAsString(value)
    inline fun <reified T> read(value: String) = jacksonObjectMapper().readValue<T>(value)
}

