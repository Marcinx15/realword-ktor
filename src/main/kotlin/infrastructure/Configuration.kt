package com.example.infrastructure

import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlin.time.Duration

@Serializable
data class Configuration(
    val ktor: Ktor,
    val jwt: Jwt,
    val db: Db
) {
    @Serializable
    data class Ktor(val host: String, val port: Int)

    @Serializable
    data class Jwt(val issuer: String, val audience: String, val expiration: Duration, val secret: String)

    @Serializable
    data class Db(val url: String, val username: String, val password: String)

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun loadFromFile(): Configuration =
            Hocon.Default.decodeFromConfig(
                ConfigFactory.parseResources("config/application.conf").resolve()
            )
    }
}
