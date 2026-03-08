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
