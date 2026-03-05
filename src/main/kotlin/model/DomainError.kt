package com.example.model

sealed interface DomainError

sealed interface UserError: DomainError
object UserNotFound : UserError
object IncorrectPassword : UserError


