package me.gubin.simple.service.routings

import me.gubin.simple.service.persistence.domains.Role
import me.gubin.simple.service.persistence.domains.RoleName
import java.util.*

data class CreateAccountRequest(
    val username: String,
    val password: String,
    val roleName: RoleName
) {
    init {
        require(username.isNotBlank()) { "'username' can't be blank" }
        require(password.isNotBlank()) { "'password' can't be blank" }
    }
}

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
) {
    init {
        require(currentPassword.isNotBlank()) { "'current password' can't be blank" }
        require(newPassword.isNotBlank()) { "'new password' can't be blank" }
    }
}

data class AccountView(
    val uuid: UUID,
    val username: String,
    val role: Role
)