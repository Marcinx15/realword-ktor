package com.example

import com.example.routes.userRoutes
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) { json() }
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
    }
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        route(path = "api") { openApi() }
        route(path = "docs") { swaggerUI(openApiUrl = "/api") }
        userRoutes()
    }
}
