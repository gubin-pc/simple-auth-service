package me.gubin.simple.service.routings

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import me.gubin.simple.service.AUTH_NAME
import me.gubin.simple.service.SESSION
import me.gubin.simple.service.accountService
import me.gubin.simple.service.persistence.domains.Account
import me.gubin.simple.service.persistence.domains.RoleName.Admin
import me.gubin.simple.service.persistence.domains.RoleName.Reviewer
import me.gubin.simple.service.persistence.domains.RoleName.User
import me.gubin.simple.service.plugins.withRole
import org.jetbrains.exposed.exceptions.ExposedSQLException


fun Application.configureApiRouting() {


    routing {

        post("/sign-up") {
            val request = call.receive<CreateAccountRequest>()
            val account = accountService.create(request.username, request.password, request.roleName)
            call.respond(HttpStatusCode.Created, account.toView())
        }

        authenticate(AUTH_NAME) {
            post("/sign-in") {
                val account = call.principal<Account>()!!
                call.sessions.set(Session(account.uuid, account.username, "", account.role))
                call.respond(HttpStatusCode.OK, account.toView())
            }
        }

        authenticate(AUTH_NAME, SESSION) {

            post("/logout") {
                call.sessions.clear<Session>()
                call.respond(HttpStatusCode.OK)
            }

            post("/change-password") {
                val account = call.principal<Account>()!!
                val request = call.receive<ChangePasswordRequest>()
                if (accountService.changePassword(account.username, request.currentPassword, request.newPassword)) {
                    call.sessions.clear<Session>()
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Conflict)
                }
            }

            withRole(Admin) {
                get("/admin") {
                    val account = call.principal<Account>()
                    call.respondText { "You role: ${account?.role}\n" +
                            "Endpoint was call: ${call.url()}" }
                }
            }

            withRole(Reviewer) {
                get("/reviewer") {
                    val account = call.principal<Account>()
                    call.respondText { "You role: ${account?.role}\n" +
                            "Endpoint was call: ${call.url()}" }
                }

                get("/accountant") {
                    val account = call.principal<Account>()
                    call.respondText { "You role: ${account?.role}\n" +
                            "Endpoint was call: ${call.url()}" }
                }
            }

            withRole(User) {
                get("/user") {
                    val account = call.principal<Account>()
                    call.respondText { "You role: ${account?.role}\n" +
                            "Endpoint was call: ${call.url()}" }
                }
            }
        }

    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<ExposedSQLException>() { call, ex ->
            when (ex.errorCode) {
                23505 -> call.respond(HttpStatusCode.Conflict)
                else -> call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

}


private fun Account.toView(): AccountView {
    return AccountView(
        this.uuid,
        this.username,
        this.role
    )
}