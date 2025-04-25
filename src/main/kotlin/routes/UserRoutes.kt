package com.example.routes

import com.example.model.UserRequest
import com.example.model.UserResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.response.respond

fun Route.userRoutes() {
    post<UserRequest>(path = "/api/users", builder = postUserDocs) { user ->
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

    get(path = "/api/user", builder = getUserDocs) {
        call.respond(
            UserResponse(
                email = "marcin@gmail.com",
                token = "123456789",
                username = "marcin123",
                bio = "whatever",
                image = "pretty boy elo"
            )
        )
    }
}

private val postUserDocs: RouteConfig.() -> Unit = {
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

private val getUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Get current user"
    response {
        HttpStatusCode.OK to {
            description = "Current user"
            body<UserResponse> {}
        }
    }
}