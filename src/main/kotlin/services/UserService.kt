package com.example.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.raise.withError
import arrow.core.raise.zipOrAccumulate
import com.example.model.Blank
import com.example.model.DomainError
import com.example.model.Email
import com.example.model.IncorrectPassword
import com.example.model.InvalidInput
import com.example.model.User
import com.example.model.UserId
import com.example.model.UserNotFound
import com.example.model.Username
import com.example.model.ValidationError
import com.example.persistence.HashedPassword
import com.example.persistence.UserRepo
import org.mindrot.jbcrypt.BCrypt

class UserService(val userRepo: UserRepo, val jwtService: JwtService) {
    fun registerUser(registerUserCommand: RegisterUserCommand): Either<DomainError, UserWithToken> = either {
        val (username, email, plainPassword) = registerUserCommand.validate().bind()
        val userId = userRepo.createUser(
            username = username,
            email = email,
            passwordHash = hashPassword(plainPassword)
        )

        val jwtToken = jwtService.createToken(userId)

        UserWithToken(
            user = User(
                username = username,
                email = email,
                bio = null,
                image = null
            ),
            token = jwtToken
        )
    }


    fun login(loginCommand: LoginCommand): Either<DomainError, UserWithToken> = either {
        val (email, plainPassword) = loginCommand.validate().bind()
        val userForAuth = userRepo.getUserByEmailForAuth(email)
        val (userId, user, hashedPassword) = ensureNotNull(userForAuth) { UserNotFound }
        ensure(BCrypt.checkpw(plainPassword.value, hashedPassword.value)) { IncorrectPassword }

        val jwtToken = jwtService.createToken(userId)

        UserWithToken(user = user, token = jwtToken)
    }

    fun getUserData(userId: UserId): Either<UserNotFound, User> = either {
        ensureNotNull(userRepo.getUserById(userId)) { UserNotFound }
    }

    private fun hashPassword(password: PlainPassword): HashedPassword =
        HashedPassword(BCrypt.hashpw(password.value, BCrypt.gensalt()))
}


data class UserWithToken(val user: User, val token: JwtToken)

@JvmInline
value class PlainPassword private constructor(val value: String) {
    companion object {
        const val FIELD_NAME = "password"
        operator fun invoke(rawPassword: String): Either<ValidationError, PlainPassword> = either {
            rawPassword.trim().let {
                ensure(it.isNotEmpty()) { Blank(FIELD_NAME) }
                PlainPassword(it)
            }
        }
    }
}

data class RegisterUserCommand(val username: String, val email: String, val password: String) {
    fun validate(): Either<InvalidInput, Triple<Username, Email, PlainPassword>> = either {
        withError(::InvalidInput) {
            zipOrAccumulate(
                { Username(username).bind() },
                { Email(email).bind() },
                { PlainPassword(password).bind() },
            ) { a, b, c -> Triple(a, b, c) }
        }
    }
}

data class LoginCommand(val email: String, val password: String) {
    fun validate(): Either<InvalidInput, Pair<Email, PlainPassword>> = either {
        withError(::InvalidInput) {
            zipOrAccumulate(
                { Email(email).bind() },
                { PlainPassword(password).bind() }
            ) { a, b -> Pair(a, b) }
        }
    }
}
