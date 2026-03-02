package com.example.routes

import com.example.model.UserId
import com.example.routes.dto.RegisterUserRequest
import com.example.routes.dto.UserResponse
import com.example.services.UserService
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpHeaders
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

fun Route.userRoutes(userService: UserService) {
    post<RegisterUserRequest>(path = "/api/users", builder = postUserDocs) {
        val response = userService.registerUser(it).let { result ->
            UserResponse(
                email = result.user.email.value,
                token = result.token.value,
                username = result.user.username.value,
                bio = null,
                image = null
            )
        }
        call.respond(HttpStatusCode.Created, response)
    }

    authenticate {
        get(path = "/api/user", builder = getUserDocs) {
            val userId = call.principal<UserId>()!!
            val token = (call.request.parseAuthorizationHeader() as? HttpAuthHeader.Single)?.blob!!
            val response = userService.getUserData(userId).let { user ->
                UserResponse(
                    email = user.email.value,
                    token = token,
                    username = user.username.value,
                    bio = user.bio,
                    image = user.image
                )
            }
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

private val postUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Register user"
    request { body<RegisterUserRequest>() }
    response {
        HttpStatusCode.Created to {
            description = "Success"
            body<UserResponse> { }
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