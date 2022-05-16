package me.gubin.simple.service.routings

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import me.gubin.simple.service.AUTH_NAME
import me.gubin.simple.service.SESSION
import me.gubin.simple.service.accountService
import me.gubin.simple.service.digestFunction
import me.gubin.simple.service.persistence.domains.Account
import me.gubin.simple.service.persistence.domains.Role
import java.util.*

fun Application.configureSecurity() {

    install(Sessions) {
        cookie<Session>("SESSION", SessionStorageMemory()) {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 60
        }
    }

    install(Authentication) {
        basic(AUTH_NAME) {
            validate { credentials ->
                val account = accountService.findBy(credentials.name, false)
                if (account == null || account.password != digestFunction(credentials.password).encodeBase64()) {
                    return@validate null
                }
                return@validate account
            }
        }
        session<Session>(SESSION) {
            validate { session ->
                return@validate accountService.findBy(session.username)
            }
            challenge {
                call.respondRedirect("/")
            }
        }

    }

}

data class Session(
    override var uuid: UUID,
    override val username: String,
    override val password: String = "",
    override val role: Role
): Account(uuid, username, password, role)
