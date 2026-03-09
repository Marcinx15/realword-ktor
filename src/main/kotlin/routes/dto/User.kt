package com.example.routes.dto

import com.example.services.UserWithToken
import kotlinx.serialization.Serializable

@Serializable
data class RegisterUserRequest(val user: RegisterUserRequestData) {
    @Serializable
    data class RegisterUserRequestData(val username: String, val email: String, val password: String)
}

@Serializable
data class UserResponse(val user: UserResponseData) {
    @Serializable
    data class UserResponseData(
        val email: String,
        val token: String,
        val username: String,
        val bio: String?,
        val image: String?
    )

    companion object {
        operator fun invoke(email: String, token: String, username: String, bio: String?, image: String?) =
            UserResponse(UserResponseData(email, token, username, bio, image))
    }
}


fun UserWithToken.toUserResponse() = UserResponse(
    email = user.email.value,
    token = token.value,
    username = user.username.value,
    bio = user.bio,
    image = user.image
)

@Serializable
data class LoginRequest(val user: LoginRequestData) {
    @Serializable
    data class LoginRequestData(val email: String, val password: String)
}