package me.gubin.simple.service

import io.kotest.common.runBlocking
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.ProjectListener
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.gubin.simple.service.persistence.configureDatabase
import me.gubin.simple.service.routings.configureApiRouting


class TestProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(KtorServerListener)

    override val includeTestScopePrefixes = true

}

object KtorServerListener : ProjectListener {

    private val embeddedServer = embeddedServer(Netty, port = 0) {
        configurePlugins()
        configureDatabase()
        configureSecurity()
        configureApiRouting()
    }

    override suspend fun beforeProject() {
            embeddedServer.start()
    }

    override suspend fun afterProject() {
            embeddedServer.stop()
    }

    fun getServerPort() = runBlocking { embeddedServer.resolvedConnectors().first().port }

}