package com.example.model

import arrow.core.Either
import arrow.core.raise.context.ensure
import arrow.core.raise.either

@JvmInline
value class Email private constructor(val value: String) {
    companion object {
        const val FIELD_NAME = "email"

        operator fun invoke(rawEmail: String): Either<ValidationError, Email> = either {
            rawEmail.trim().let {
                ensure(it.isNotEmpty()) { Blank(FIELD_NAME) }
                Email(it)
            }
        }

        internal fun fromDb(rawEmail: String) = Email(rawEmail)
    }
}

@JvmInline
value class Username private constructor(val value: String) {
    companion object {
        const val FIELD_NAME = "username"

        operator fun invoke(rawUsername: String): Either<ValidationError, Username> = either {
            rawUsername.trim().let {
                ensure(it.isNotEmpty()) { Blank(FIELD_NAME) }
                Username(it)
            }
        }

        internal fun fromDb(rawUsername: String) = Username(rawUsername)
    }
}

data class User(
    val username: Username,
    val email: Email,
    val bio: String?,
    val image: String?,
)

@JvmInline
value class UserId(val value: Int)



