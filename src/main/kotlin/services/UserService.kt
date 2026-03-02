package com.example.services

import com.example.model.Email
import com.example.model.User
import com.example.model.UserId
import com.example.model.Username
import com.example.persistence.UserRepo
import com.example.routes.dto.RegisterUserRequest
import org.mindrot.jbcrypt.BCrypt

data class RegisterUserResult(val user: User, val token: JwtToken)

class UserService(val userRepo: UserRepo, val jwtService: JwtService) {

    fun registerUser(request: RegisterUserRequest): RegisterUserResult {
        // validate request data here
        val passwordHash: String = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val userId = userRepo.createUser(
            username = request.username,
            email = request.email,
            passwordHash = passwordHash
        )

        val jwtToken = jwtService.createToken(userId.value)

        return RegisterUserResult(
            user = User(
                username = Username(request.username),
                email = Email(request.email),
                bio = null,
                image = null
            ),
            token = jwtToken
        )
    }

    fun getUserData(userId: UserId): User = userRepo.getUser(userId)

}