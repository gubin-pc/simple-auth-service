package me.gubin.simple.service.routings

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import me.gubin.simple.service.AUTH_NAME
import me.gubin.simple.service.SESSION
import me.gubin.simple.service.accountService
import me.gubin.simple.service.plugins.withRole
import me.gubin.simple.service.persistence.domains.Account


fun Application.configureRouting() {

    routing {

        static("/") {
            staticBasePackage = "static"
            resource("index.html")
            defaultResource("index.html")
            static("js") {
                resources("js")
            }
            static("css") {
                resources("css")
            }
        }

        post("/sign_up") {
            val request = call.receive<CreateAccountRequest>()
            val response = accountService.create(
                request.username,
                request.password,
                request.role
            )
            call.respond(HttpStatusCode.Created, response.toView())
        }

        authenticate(AUTH_NAME, SESSION) {
            post("/sign_in") {
                val account = call.principal<Account>()!!
                call.sessions.set(Session(account.uuid, account.username, "", account.role))
                when(account.role.name) {
                    "Admin" -> call.respondRedirect("/admin")
                    "Reviewer" -> call.respondRedirect("/reviewer")
                    "User" -> call.respondRedirect("/user")
                }
            }
            post("/logout") {
                call.sessions.clear<Session>()
                call.respondRedirect("/sign_in")
            }
            post("/change_password") {
                val request = call.receive<ChangePasswordRequest>()
                accountService.update(request.username, request.newPassword)
                call.sessions.clear<Session>()
                call.respond(HttpStatusCode.OK)
            }
            withRole("Admin") {
                get("/admin") {
                    call.respondText { "Admin" }
                }
            }
            withRole("Reviewer") {
                get("/reviewer") {
                    call.respondText { "Reviewer" }
                }
            }
            withRole("User") {
                get("/user") {
                    call.respondText { "User" }
                }
            }
        }

    }
}

private fun Account.toView(): AccountView {
    return AccountView(
        this.uuid,
        this.username,
        this.role.name
    )
}
