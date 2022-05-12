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
        }

        context("sing in cases") {
            should("sing in success as Admin") {
                withAccount(role = "Admin") {account, password ->
                    client.post("/sign_in") {
                        basicAuth(account.username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.Found
                        headers[HttpHeaders.Location] shouldBe "/admin"
                    }
                }
            }

            should("sing in success as User") {
                withAccount(role = "User") {account, password ->
                    client.post("/sign_in") {
                        basicAuth(account.username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.Found
                        headers[HttpHeaders.Location] shouldBe "/user"
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
                withAccount { account, password ->
                    client.get("/admin") {
                        basicAuth(account.username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("have access to user endpoint as Admin") {
                withAccount { account, password ->
                    client.get("/user") {
                        basicAuth(account.username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("have access to reviewer endpoint as Reviewer") {
                withAccount(role = "Reviewer") { account, password ->
                    client.get("/reviewer") {
                        basicAuth(account.username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.OK
                    }
                }
            }

            should("don't have access to admin endpoint as Reviewer") {
                withAccount(role = "Reviewer") { account, password ->
                    client.get("/admin") {
                        basicAuth(account.username, password)
                    }.apply {
                        status shouldBe HttpStatusCode.Forbidden
                    }
                }
            }
        }

        should("change password success"){
            withAccount { account, password ->
                client.post("/sign_in") {
                    basicAuth(account.username, password)
                }.apply {
                    status shouldBe HttpStatusCode.Found
                    headers[HttpHeaders.Location] shouldBe "/admin"
                }

                val newPassword = "newPassword"
                client.post("/change_password") {
                    basicAuth(account.username, password)
                    contentType(ContentType.Application.Json)
                    setBody("""
                        {
                          "username" : "${account.username}",
                          "newPassword" : "$newPassword"
                        }
                    """.trimIndent())
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }

                client.post("/sign_in") {
                    basicAuth(account.username, newPassword)
                }.apply {
                    status shouldBe HttpStatusCode.Found
                    headers[HttpHeaders.Location] shouldBe "/admin"
                }
            }
        }

        should("make all operation with sesstion") {
            val client = HttpClient(CIO) {
                install(HttpCookies)
                defaultRequest {
                    url("http://localhost:8080/")
                }
            }

            withAccount {account, password ->
                val result = client.post("/sign_in") {
                    basicAuth(account.username, password)
                }
                result.status shouldBe HttpStatusCode.Found
                result.headers[HttpHeaders.Location] shouldBe "/admin"
                val cookie = result.setCookie().first { it.name == "SESSION" }.shouldNotBeNull()

                client.get("/admin") {
                    cookie(cookie.name, cookie.value, cookie.maxAge, cookie.expires, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly, cookie.extensions)
                }.apply {
                    status shouldBe HttpStatusCode.OK
                }

                client.post("/logout") {
                    cookie(cookie.name, cookie.value, cookie.maxAge, cookie.expires, cookie.domain, cookie.path, cookie.secure, cookie.httpOnly, cookie.extensions)
                }.apply {
                    status shouldBe HttpStatusCode.Found
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