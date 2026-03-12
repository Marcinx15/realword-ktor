package com.example.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.UserId
import com.example.routes.dto.FieldError
import com.example.services.JwtToken
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.response.respond

data class JwtPrincipal(val userId: UserId, val token: JwtToken)

fun Application.installAuthentication() {
    val jwtVerifier = Dependencies.config.jwt.let {
        JWT.require(Algorithm.HMAC256(it.secret))
            .withAudience(it.audience)
            .withIssuer(it.issuer)
            .build()
    }

    install(Authentication) {
        jwt {
            authSchemes("Token", "Bearer")
            verifier(jwtVerifier)
            validate { credential ->
                val userId = credential.subject?.toIntOrNull()?.let { UserId(it) }
                val token = (request.parseAuthorizationHeader() as? HttpAuthHeader.Single)?.blob

                if (userId != null && token != null) JwtPrincipal(userId, JwtToken(token))
                else null
            }
            challenge { _, _ ->
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                if (authHeader == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        FieldError("token", "is missing")
                    )
                } else call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}
