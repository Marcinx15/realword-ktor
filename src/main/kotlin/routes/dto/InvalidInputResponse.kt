package com.example.routes.dto

import com.example.model.InvalidInput
import kotlinx.serialization.Serializable

@Serializable
data class InvalidInputResponse(val errors: Map<String, List<String>>) {
    companion object {
        fun from(invalidInput: InvalidInput): InvalidInputResponse =
            InvalidInputResponse(
                invalidInput.validationErrors.groupBy(
                    keySelector = { it.fieldName },
                    valueTransform = { it.message }
                )
            )
    }
}

@Serializable
class FieldError private constructor(val errors: Map<String, List<String>>) {
    companion object {
        operator fun invoke(fieldName: String, errorMessage: String): FieldError =
            FieldError(mapOf(fieldName to listOf(errorMessage)))
    }
}
