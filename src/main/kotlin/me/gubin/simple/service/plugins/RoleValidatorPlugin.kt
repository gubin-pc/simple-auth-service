package me.gubin.simple.service.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import me.gubin.simple.service.persistence.domains.Account
import me.gubin.simple.service.persistence.domains.Role
import me.gubin.simple.service.persistence.domains.RoleName
import me.gubin.simple.service.roleService

@KtorDsl
class RoleConfig {
    internal var role: Role? = null
}

class RoleValidatorRouteSelector : RouteSelector() {
    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
        return RouteSelectorEvaluation.Transparent
    }
}

val RoleValidatorPlugin: RouteScopedPlugin<RoleConfig> =
    createRouteScopedPlugin("RoleValidation", ::RoleConfig) {
        on(AuthenticationChecked) { call ->
            if (call.authentication.allFailures.isEmpty()) {
                val userRole = call.authentication.principal<Account>()?.role
                if (pluginConfig.role!! >= userRole) {
                    return@on
                }
                call.respond(ForbiddenResponse())
            }
        }
    }

fun Route.withRole(roleName: RoleName, build: Route.() -> Unit): Route {
    val authenticatedRoute = createChild(RoleValidatorRouteSelector())
    authenticatedRoute.install(RoleValidatorPlugin) {
        this.role = roleService.findByName(roleName)
    }
    authenticatedRoute.build()
    return authenticatedRoute
}