package me.gubin.simple.service.routings

import me.gubin.simple.service.persistence.domains.Account
import me.gubin.simple.service.persistence.domains.Role
import java.util.*

data class Session(
    override var uuid: UUID,
    override val username: String,
    override val password: String = "",
    override val role: Role
): Account(uuid, username, password, role)