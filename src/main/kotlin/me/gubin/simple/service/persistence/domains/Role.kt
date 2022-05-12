package me.gubin.simple.service.persistence.domains

import me.gubin.simple.service.persistence.Roles
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

class RoleDomain(id: EntityID<Int>) : IntEntity(id) {
    companion object : EntityClass<Int, RoleDomain>(Roles)

    var name by Roles.name
    var priority by Roles.priority
}

data class Role(
    val name: String,
    val priority: Int
) {
    operator fun compareTo(role: Role?) = this.priority - (role?.priority ?: Int.MAX_VALUE)
}

fun RoleDomain.toModel(): Role {
    return Role(
        this.name,
        this.priority
    )
}


