package me.gubin.simple.service

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import me.gubin.simple.service.routings.Session
import me.gubin.simple.service.services.AccountService
import me.gubin.simple.service.services.RoleService
import org.slf4j.event.Level

const val AUTH_NAME = "main"
const val SESSION = "session"

val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }
val accountService = AccountService()
val roleService = RoleService()

fun Application.configurePlugins() {
    install(ContentNegotiation) {
        jackson()
    }
    install(CallLogging) {
        level = Level.INFO
    }
}

fun Application.configureSecurity() {

    install(StatusPages) {
        exception<IllegalArgumentException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest)
        }
    }

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
                call.respondRedirect("/login")
            }
        }

    }

}
