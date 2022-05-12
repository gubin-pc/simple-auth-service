package me.gubin.simple.service.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.configureDatabase() {

    val hikariConfig = HikariConfig()
    hikariConfig.username = "root"
    hikariConfig.password = "root"
    hikariConfig.jdbcUrl = "jdbc:h2:mem:test"
    hikariConfig.maximumPoolSize = 20
    Database.connect(HikariDataSource(hikariConfig))

    transaction {

        log.info("Apply database schema if need (migration imitation)")

        SchemaUtils.create(Accounts, Roles)

        Roles.insert {
            it[this.name] = "Admin"
            it[this.priority] = 0
        }
        Roles.insert {
            it[this.name] = "Reviewer"
            it[this.priority] = 100
        }
        Roles.insert {
            it[this.name] = "User"
            it[this.priority] = 200
        }
        commit()

        log.info("Applying database schema is finished")
    }
}