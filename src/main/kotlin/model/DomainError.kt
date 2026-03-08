package com.example.model

import arrow.core.NonEmptyList

sealed interface DomainError

sealed interface UserError : DomainError
object UserNotFound : UserError
object IncorrectPassword : UserError

data class InvalidInput(val validationErrors: NonEmptyList<ValidationError>) : DomainError


// move it somewhere else as it does not implement DomainError
sealed interface ValidationError {
    val fieldName: String
    val message: String
}

data class Blank(override val fieldName: String): ValidationError {
    override val message: String = "can't be blank"
}

