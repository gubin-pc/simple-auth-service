package me.gubin.simple.service

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.gubin.simple.service.routings.AccountView
import me.gubin.simple.service.routings.CreateAccountRequest

class ApplicationIT : IntegrationSpec() {

    init {

        context("sign up cases") {

            should("sign up success") {
                val request = CreateAccountRequest("test", "test", "User")
                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody(write(request))
                }.apply {
                    status shouldBe HttpStatusCode.Created
                    val payload = read<AccountView>(bodyAsText())
                    payload.username shouldBe request.username
                    payload.role shouldBe request.role
                }
            }

            should("sign up failed with empty username") {
                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : " ",
                          "password" : "test",
                          "role" : "User"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.BadRequest
                }
            }

            should("sign up failed with empty password") {
                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : "test",
                          "password" : " ",
                          "role" : "User"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.BadRequest
                }
            }

            should("sign up failed with empty role") {
                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : "test",
                          "password" : "test",
                          "role" : " "
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.BadRequest
                }
            }

            should("sign up failed with wrong role") {
                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : "test",
                          "password" : "test",
                          "role" : "Reader"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.BadRequest
                }
            }

            should("sign up failed because long username") {
                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : "${List(71) { "a" }.joinToString()}",
                          "password" : "test",
                          "role" : "Reader"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.BadRequest
                }
            }

            should("sign up failed because not uniq") {
                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : "uniq",
                          "password" : "test",
                          "role" : "Admin"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.Created
                }

                client.post("/sign_up") {
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : "uniq",
                          "password" : "test",
                          "role" : "Admin"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.Conflict
                }
            }
        }

        context("sing in cases") {
            should("sing in success as Admin") {
                withAccount(role = "Admin") { username, password, _ ->
                    client.post("/sign_in") {
                        basicAuth(username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("sing in success as User") {
                withAccount(role = "User") { username, password, _ ->
                    client.post("/sign_in") {
                        basicAuth(username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("sing in failed as unknown account") {
                client.post("/sign_in") {
                    basicAuth("unknown", "unknown")
                }.apply {
                    status shouldBe HttpStatusCode.Unauthorized
                }
            }
        }

        context("check access") {
            should("have access to admin endpoint as Admin") {
                withAccount { username, password, _ ->
                    client.get("/admin") {
                        basicAuth(username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("have access to user endpoint as Admin") {
                withAccount { username, password, _ ->
                    client.get("/user") {
                        basicAuth(username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("have access to reviewer endpoint as Reviewer") {
                withAccount(role = "Reviewer") { username, password, _ ->
                    client.get("/reviewer") {
                        basicAuth(username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("don't have access to admin endpoint as Reviewer") {
                withAccount(role = "Reviewer") { username, password, _ ->
                    client.get("/admin") {
                        basicAuth(username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.Forbidden
                    }
                }
            }
        }

        should("change password success"){
            withAccount { username, password, _ ->
                client.post("/sign_in") {
                    basicAuth(username, password)
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }

                val newPassword = "newPassword"
                client.post("/change_password") {
                    basicAuth(username, password)
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "currentPassword" : "$password",
                          "newPassword" : "$newPassword"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }

                client.post("/sign_in") {
                    basicAuth(username, newPassword)
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }
            }
        }

        should("change password failed if current password incorrect"){
            withAccount { username, password, _ ->
                client.post("/sign_in") {
                    basicAuth(username, password)
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }

                val newPassword = "newPassword"
                client.post("/change_password") {
                    basicAuth(username, password)
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "currentPassword" : "incorrent$password",
                          "newPassword" : "$newPassword"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.BadRequest
                }
            }
        }

        should("make all operation with session") {
            val client = HttpClient(CIO) {
                install(HttpCookies)
                defaultRequest {
                    url("http://localhost:$serverPort")
                }
            }

            withAccount { username, password, _ ->
                val result = client.post("/sign_in") {
                    basicAuth(username, password)
                }
                result.status shouldBe HttpStatusCode.OK
                val cookie = result.setCookie().first { it.name == "SESSION" }.shouldNotBeNull()

                client.get("/admin") {
                    cookie(cookie.name, cookie.value, cookie.maxAge, cookie.expires, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly, cookie.extensions)
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }

                client.post("/logout") {
                    cookie(cookie.name, cookie.value, cookie.maxAge, cookie.expires, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly, cookie.extensions)
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }

                client.get("/admin") {
                    cookie(cookie.name, cookie.value, cookie.maxAge, cookie.expires, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly, cookie.extensions)
                }.apply {
                    status shouldBe HttpStatusCode.Unauthorized
                }
            }
        }
    }

}