package com.example.infrastructure


import com.example.routes.userRoutes
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.AuthScheme
import io.github.smiley4.ktoropenapi.config.AuthType
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

fun Application.configure() {
    installContentNegotiation()
    installDocs()
    installCallLogging()
    installStatusPages()
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


private fun Application.installContentNegotiation() {
    install(ContentNegotiation) { json(Json { explicitNulls = true }) }
}

private fun Application.installCallLogging() {
    install(CallLogging)
}

private fun Application.installStatusPages() {
    install(StatusPages) {
        exception<ContentTransformationException> { call, cause ->
            logger.warn(cause) { "Content transform error: ${cause.message}" }
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<BadRequestException> { call, cause ->
            logger.warn(cause) { "Bad request: ${cause.message}" }
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<SerializationException> { call, cause ->
            logger.warn(cause) { "Serialization error: ${cause.message}" }
            call.respond(HttpStatusCode.BadRequest)
        }
        exception<JsonConvertException> { call, cause ->
            logger.warn(cause) { "Json convert error: ${cause.message}" }
            call.respond(HttpStatusCode.BadRequest)
        }
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
