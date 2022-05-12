package me.gubin.simple.service.routings

import java.util.UUID

data class CreateAccountRequest(
    val username: String,
    val password: String,
    val role: String
) {
    init {
        require(username.isNotBlank()) { "'username' can't be blank" }
        require(password.isNotBlank()) { "'password' can't be blank" }
        require(role.isNotBlank()) { "'role' can't be blank" }
    }
}

data class ChangePasswordRequest(
    val username: String,
    val newPassword: String
) {
    init {
        require(username.isNotBlank()) { "'username' can't be blank" }
        require(newPassword.isNotBlank()) { "'password' can't be blank" }
    }
}

data class AccountView(
    val uuid: UUID,
    val username: String,
    val role: String
)