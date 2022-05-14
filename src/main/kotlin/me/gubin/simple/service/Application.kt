package me.gubin.simple.service

import io.ktor.server.application.*
import io.ktor.server.netty.*
import me.gubin.simple.service.persistence.configureDatabase
import me.gubin.simple.service.routings.configureApiRouting
import me.gubin.simple.service.routings.configureStaticRouting

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.modules() {
    configurePlugins()
    configureDatabase()
    configureSecurity()
    configureApiRouting()
    configureStaticRouting()
}

