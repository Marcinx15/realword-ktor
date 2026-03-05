package com.example.services

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.example.model.Email
import com.example.model.IncorrectPassword
import com.example.model.User
import com.example.model.UserError
import com.example.model.UserId
import com.example.model.UserNotFound
import com.example.model.Username
import com.example.persistence.UserRepo
import org.mindrot.jbcrypt.BCrypt

data class UserWithToken(val user: User, val token: JwtToken)

class UserService(val userRepo: UserRepo, val jwtService: JwtService) {

    fun registerUser(
        username: Username,
        email: Email,
        password: String
    ): Either<UserError, UserWithToken> = either {
        // validate request data here
        val passwordHash: String = hashPassword(password)
        val userId = userRepo.createUser(
            username = username,
            email = email,
            passwordHash = passwordHash
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


    fun login(email: Email, password: String): Either<UserError, UserWithToken> = either {
        val userForAuth = userRepo.getUserByEmailForAuth(email)
        val (userId, user, hashedPassword) = ensureNotNull(userForAuth) { UserNotFound }
        ensure(BCrypt.checkpw(password, hashedPassword)) { IncorrectPassword }

        val jwtToken = jwtService.createToken(userId)

        UserWithToken(user = user, token = jwtToken)
    }

    fun getUserData(userId: UserId): Either<UserNotFound, User> = either {
        ensureNotNull(userRepo.getUserById(userId)) { UserNotFound }
    }

    private fun hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt())
}
