package com.example.routes.dto

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
    val bio: String,
    val image: String
)