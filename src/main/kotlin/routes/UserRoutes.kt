package com.example.routes

import arrow.core.Either
import com.example.JwtPrincipal
import com.example.model.DomainError
import com.example.model.EmailAlreadyTaken
import com.example.model.IncorrectPassword
import com.example.model.InvalidInput
import com.example.model.UserNotFound
import com.example.model.UsernameAlreadyTaken
import com.example.routes.dto.FieldError
import com.example.routes.dto.InvalidInputResponse
import com.example.routes.dto.LoginRequest
import com.example.routes.dto.RegisterUserRequest
import com.example.routes.dto.UpdateUserRequest
import com.example.routes.dto.UserResponse
import com.example.routes.dto.toUserResponse
import com.example.services.LoginCommand
import com.example.services.RegisterUserCommand
import com.example.services.UpdateUserCommand
import com.example.services.UserService
import com.example.services.UserWithToken

import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.put
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

fun Route.userRoutes(userService: UserService) {
    post<RegisterUserRequest>(path = "/api/users", builder = registerUserDocs) { req ->
        userService.registerUser(
            RegisterUserCommand(
                username = req.user.username,
                email = req.user.email,
                password = req.user.password
            )
        )
            .map { it.toUserResponse() }
            .respond(HttpStatusCode.Created)
    }

    post<LoginRequest>(path = "/api/users/login", builder = loginDocs) { req ->
        userService.login(LoginCommand(email = req.user.email, password = req.user.password))
            .map { it.toUserResponse() }
            .respond(HttpStatusCode.OK)
    }

    authenticate {
        get(path = "/api/user", builder = getCurrentUserDocs) {
            val (userId, token) = call.principal<JwtPrincipal>()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            userService.getUser(userId)
                .map { UserWithToken(it, token).toUserResponse() }
                .respond(HttpStatusCode.OK)
        }

        put<UpdateUserRequest>(path = "/api/user", builder = updateUserDocs) { req ->
            val (userId, token) = call.principal<JwtPrincipal>()
                ?: return@put call.respond(HttpStatusCode.Unauthorized)

            val updateUserCommand = UpdateUserCommand(
                userId = userId,
                username = req.user.username,
                email = req.user.email,
                password = req.user.password,
                bio = req.user.bio,
                image = req.user.image
            )

            userService.updateUser(updateUserCommand)
                .map { UserWithToken(it, token).toUserResponse() }
                .respond(HttpStatusCode.OK)
        }
    }
}

context(routingContext: RoutingContext)
private suspend inline fun <reified T : Any> Either<DomainError, T>.respond(status: HttpStatusCode) {
    routingContext.apply {
        fold(
            ifRight = { call.respond(status, it) },
            ifLeft = {
                when (it) {
                    is InvalidInput -> call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        InvalidInputResponse.from(it)
                    )

                    is IncorrectPassword -> call.respond(
                        HttpStatusCode.Unauthorized,
                        FieldError(fieldName = "credentials", errorMessage = "invalid")
                    )

                    is UserNotFound -> call.respond(
                        HttpStatusCode.NotFound,
                        FieldError(fieldName = it.byProperty, errorMessage = "user not found")
                    )

                    EmailAlreadyTaken -> call.respond(
                        HttpStatusCode.Conflict,
                        FieldError(fieldName = "email", errorMessage = "has already been taken")
                    )

                    UsernameAlreadyTaken -> call.respond(
                        HttpStatusCode.Conflict,
                        FieldError(fieldName = "username", errorMessage = "username has already taken")
                    )
                }
            }
        )
    }
}


private val registerUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Register user"
    request { body<RegisterUserRequest>() }
    response {
        HttpStatusCode.Created to {
            description = "Success"
            body<UserResponse> {}
        }
        HttpStatusCode.BadRequest to {
            description = "An invalid request"
        }
    }
}

private val loginDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Login"
    request { body<LoginRequest>() }
    response {
        HttpStatusCode.OK to {
            description = "Success"
            body<UserResponse> {}
        }
    }
}

private val getCurrentUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Get current user"
    response {
        HttpStatusCode.OK to {
            description = "Current user"
            body<UserResponse> {}
        }
    }
}

private val updateUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Update current user"
    response {
        HttpStatusCode.OK to {
            description = "Current user"
            body<UserResponse> {}
        }
    }
}
