package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.UserId
import com.example.persistence.UserRepo
import com.example.routes.userRoutes
import com.example.services.JwtService
import com.example.services.JwtToken
import com.example.services.UserService
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import java.time.Clock
import javax.sql.DataSource
import kotlin.apply
import kotlin.time.Duration

fun main() {
    runDbMigrations(Dependencies.dataSource)
    val ktorConfig = Dependencies.config.ktor
    embeddedServer(Netty, port = ktorConfig.port, host = ktorConfig.host) {
        mainModule()
    }.start(wait = true)
}

fun Application.mainModule() {
    installContentNegotiation()
    installDocs()
    installAuthentication()
    configureRouting()
}

private fun Application.configureRouting() {
    routing {
        route(path = "api") { openApi() }
        route(path = "docs") { swaggerUI(openApiUrl = "/api") }
        userRoutes(Dependencies.userService)
    }
}

private fun Application.installDocs() {
    install(plugin = OpenApi) {
        outputFormat = OutputFormat.YAML
        server {
            url = "http://localhost:8080"
            description = "Local API"
        }
        info {
            title = "RealWorld in Ktor"
            description = "Implementation of https://github.com/gothinkster/realworld"
            version = "1.0.0"
        }
        security {
            defaultSecuritySchemeNames = listOf("Bearer")
            securityScheme("Bearer") {
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
            }
        }
    }
}

private fun Application.installContentNegotiation() {
    install(ContentNegotiation) { json() }
}


data class JwtPrincipal(val userId: UserId, val token: JwtToken)

private fun Application.installAuthentication() {
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
        }
    }
}

private fun runDbMigrations(dataSource: DataSource) {
    Flyway.configure().dataSource(dataSource).load().migrate()
}

object Dependencies {
    val config = Configuration.loadFromFile()
    val dataSource = HikariDataSource().apply {
        jdbcUrl = config.db.url
        username = config.db.username
        password = config.db.password
    }
    val database = Database.connect(dataSource)
    val userRepo: UserRepo = UserRepo(database)
    val jwtService: JwtService = JwtService(config.jwt, Clock.systemDefaultZone())
    val userService: UserService = UserService(userRepo, jwtService)
}

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
            Hocon.decodeFromConfig(
                ConfigFactory.parseResources("config/application.conf").resolve()
            )
    }
}

