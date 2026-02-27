package com.example.services

import com.example.persistence.UserRepo
import com.example.routes.dto.RegisterUserRequest
import org.mindrot.jbcrypt.BCrypt

class UserService(val userRepo: UserRepo) {
    fun registerUser(request: RegisterUserRequest) {
        val passwordHash: String = BCrypt.hashpw(request.password, BCrypt.gensalt())
        userRepo.createUser(
            username = request.username,
            email = request.email,
            passwordHash = passwordHash
        )
    }
}