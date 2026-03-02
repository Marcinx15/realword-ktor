package com.example.model

@JvmInline value class Email(val value: String)
@JvmInline value class Username(val value: String)

data class User(
    val username: Username,
    val email: Email,
    val bio: String?,
    val image: String?,
)

@JvmInline value class UserId(val value: Int)



