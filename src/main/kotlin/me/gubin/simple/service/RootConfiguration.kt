package me.gubin.simple.service

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.mustache.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.util.*
import me.gubin.simple.service.services.AccountService
import me.gubin.simple.service.services.RoleService
import org.slf4j.event.Level

const val AUTH_NAME = "main"
const val SESSION = "session"

val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }
val accountService = AccountService()
val roleService = RoleService()

fun Application.configurePlugins() {
    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }
    install(CallLogging) {
        level = Level.INFO
    }
    install(ContentNegotiation) {
        jackson()
    }
}