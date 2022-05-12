package me.gubin.simple.service.persistence

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID

object Accounts: IdTable<UUID>() {
    override val id: Column<EntityID<UUID>> = uuid("id").entityId()
    val username = varchar("username", length = 70).uniqueIndex()
    val password = varchar("password", length = 200)
    val role = reference("role_id", Roles)
}

object Roles: IntIdTable() {
    val name = varchar("name", length = 20)
    val priority = integer("priority")
}