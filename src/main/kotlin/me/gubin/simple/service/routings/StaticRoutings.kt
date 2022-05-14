package me.gubin.simple.service.routings

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import me.gubin.simple.service.SESSION
import me.gubin.simple.service.roleService

fun Application.configureStaticRouting() {
    routing {

        static {
            staticBasePackage = "static"
            static("/css") {
                resources("css")
            }
            static("/js") {
                resources("js")
            }
            authenticate (SESSION) {
                static("/reset") {
                    defaultResource("change_password.html")
                }
            }
        }

        get("/") {
            if (call.sessionId != null) {
                call.respondRedirect("/account")
            }
            val roles = roleService.findAll().map { it.name }
            call.respond(MustacheContent("index.hbs", mapOf("roles" to roles)))
        }

        authenticate(SESSION) {
            get("/account") {
                val roles = roleService.findAll().map { it.name }
                call.respond(MustacheContent("account.hbs", mapOf("roles" to roles)))
            }
        }

    }
}