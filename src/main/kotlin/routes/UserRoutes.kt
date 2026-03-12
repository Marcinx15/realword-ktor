package com.example.routes

import arrow.core.Either
import com.example.infrastructure.JwtPrincipal
import com.example.model.DomainError
import com.example.model.EmailAlreadyTaken
import com.example.model.EmptyUpdate
import com.example.model.IncorrectPassword
import com.example.model.InvalidInput
import com.example.model.UserNotFound
import com.example.model.UsernameAlreadyTaken
import com.example.routes.dto.FieldError
import com.example.routes.dto.InvalidInputResponse
import com.example.routes.dto.LoginRequest
import com.example.routes.dto.RegisterUserRequest
import com.example.routes.dto.UpdateUserRequest
import com.example.routes.dto.toUserResponse
import com.example.services.LoginCommand
import com.example.services.RegisterUserCommand
import com.example.services.UpdateUserCommand
import com.example.services.UserService
import com.example.services.UserWithToken

import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.put
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

fun Route.userRoutes(userService: UserService, configuredJson: Json) {
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

            fun jsonFieldUpdate(fieldName: String) =
                jsonFieldUpdate(configuredJson, req.user, fieldName, String.serializer())

            val updateUserCommand = UpdateUserCommand(
                userId = userId,
                username = jsonFieldUpdate("username"),
                email = jsonFieldUpdate("email"),
                password = jsonFieldUpdate("password"),
                bio = jsonFieldUpdate("bio"),
                image = jsonFieldUpdate("image"),
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

                    is EmailAlreadyTaken -> call.respond(
                        HttpStatusCode.Conflict,
                        FieldError(fieldName = "email", errorMessage = "has already been taken")
                    )

                    is UsernameAlreadyTaken -> call.respond(
                        HttpStatusCode.Conflict,
                        FieldError(fieldName = "username", errorMessage = "username has already taken")
                    )

                    is EmptyUpdate -> call.respond(
                        HttpStatusCode.UnprocessableEntity,
                        FieldError(fieldName = "allFields", errorMessage = "update can't be empty")
                    )
                }
            }
        )
    }
}
