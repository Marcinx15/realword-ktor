package com.example.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.UserId
import java.time.Clock
import java.time.Instant

@JvmInline value class JwtToken(val value: String)

class JwtService(val config: Configuration.Jwt, val clock: Clock) {
    fun createToken(userId: UserId): JwtToken =
        Instant.now(clock).let { now ->
            JWT.create()
                .withAudience(config.audience)
                .withIssuer(config.issuer)
                .withSubject(userId.value.toString())
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(config.expiration.inWholeSeconds))
                .sign(Algorithm.HMAC256(config.secret))
                .let { JwtToken(it) }
        }
}
