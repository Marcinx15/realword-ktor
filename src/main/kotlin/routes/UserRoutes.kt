package com.example.routes

import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

fun Route.userRoutes() {
    post<UserRequest>(
        path = "/api/users",
        builder = {
            tags = listOf("user")
            description = "Register user"
            request { body<UserRequest>() }
            response {
                HttpStatusCode.OK to {
                    description = "Success"
                    body<UserResponse> {
                        description = "The result of the operation"
                    }
                }
                HttpStatusCode.BadRequest to {
                    description = "An invalid request"
                }
            }
        }
    ) { user ->
        println(user)
        call.respond(
            UserResponse(
                email = "marcin@gmail.com",
                token = "123456789",
                username = "marcin123",
                bio = "whatever",
                image = "pretty boy"
            )
        )
    }

    get(
        path = "/api/user",
        builder = {
            tags = listOf("user")
            description = "Get current user"
            response {
                HttpStatusCode.OK to {
                    description = "Current user"
                    body<UserResponse> {}
                }
            }
        }
    ) {
        call.respond(
            UserResponse(
                email = "marcin@gmail.com",
                token = "123456789",
                username = "marcin123",
                bio = "whatever",
                image = "pretty boy"
            )
        )
    }
}

@Serializable
data class UserRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val email: String,
    val token: String,
    val username: String,
    val bio: String,
    val image: String
)