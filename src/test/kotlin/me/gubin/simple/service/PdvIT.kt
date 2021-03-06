package me.gubin.simple.service

import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import me.gubin.simple.service.persistence.domains.RoleName
import me.gubin.simple.service.routings.AccountView
import me.gubin.simple.service.routings.CreateAccountRequest
import java.util.*

class PdvIT: IntegrationSpec() {
    private val count = 100

    init {
        should("apply correctly") {
            val accounts = List(count) { Account() }
            repeat(count) {
                val account = accounts[it]

                async {
                    val newPassword = UUID.randomUUID().toString()
                    signUp(account.username, account.password, account.role)
                    signIn(account.username, account.password)
                    goToEndpoint(account.username, account.password, account.role)
                    changePassword(account.username, account.password, newPassword)
                    signIn(account.username, newPassword)
                }
            }

        }
    }

    private suspend fun signUp(username: String, password: String, roleName: RoleName) {
        val request = CreateAccountRequest(username, password, roleName)
        client.post("/sign-up") {
            contentType(ContentType.Application.Json)
            setBody(write(request))
        }.apply {
            status shouldBe HttpStatusCode.Created
            val payload = read<AccountView>(bodyAsText())
            payload.username shouldBe request.username
            payload.role.name shouldBe request.roleName
        }
    }

    private suspend fun signIn(username: String, password: String) {
        client.post("/sign-in") {
            basicAuth(username, password)
        }.apply {
            status shouldBe HttpStatusCode.OK
        }
    }

    private suspend fun changePassword(username: String, password: String, newPassword: String) {
        client.post("/change-password") {
            basicAuth(username, password)
            contentType(ContentType.Application.Json)
            setBody("""
                {
                  "currentPassword" : "$password",
                  "newPassword" : "$newPassword"
                }
            """.trimIndent())
        }.apply {
            status shouldBe HttpStatusCode.OK
        }
    }

    private suspend fun goToEndpoint(username: String, password: String, roleName: RoleName) {
        client.get("/${roleName.name.lowercase()}") {
            basicAuth(username, password)
        }.apply {
            status shouldBe HttpStatusCode.OK
        }
    }

    private data class Account (
        val username: String = UUID.randomUUID().toString(),
        val password: String = UUID.randomUUID().toString(),
        val role: RoleName = RoleName.values().random()
    )
}