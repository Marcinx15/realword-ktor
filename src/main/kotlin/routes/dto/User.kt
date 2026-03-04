package com.example.routes.dto

import com.example.services.UserWithToken
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val email: String,
    val token: String,
    val username: String,
    val bio: String?,
    val image: String?
)

fun UserWithToken.toUserResponse() = UserResponse(
    email = user.email.value,
    token = token.value,
    username = user.username.value,
    bio = user.bio,
    image = user.image
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)