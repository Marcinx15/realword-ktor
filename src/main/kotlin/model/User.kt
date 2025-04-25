package com.example.model

import kotlinx.serialization.Serializable

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

