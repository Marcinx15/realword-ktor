package com.example.routes

import com.example.application.FieldUpdate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

fun <T> jsonFieldUpdate(
    configuredJson: Json,
    obj: JsonObject,
    name: String,
    serializer: KSerializer<T>
): FieldUpdate<T?> =
    obj[name]?.let { element ->
        if (element is JsonNull) FieldUpdate.Present(null)
        else FieldUpdate.Present(configuredJson.decodeFromJsonElement(serializer, element))
    } ?: FieldUpdate.Absent
