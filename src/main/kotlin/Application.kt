package com.example

import com.example.persistence.UserRepo
import com.example.routes.userRoutes
import com.example.services.UserService
import com.zaxxer.hikari.HikariDataSource
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.OutputFormat
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import javax.sql.DataSource
import kotlin.apply

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    installContentNegotiation()
    installDocs()
    configureRouting()
    runDbMigrations(Dependencies.dataSource)
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
    }
}

private fun Application.installContentNegotiation() {
    install(ContentNegotiation) { json() }
}

private fun Application.runDbMigrations(dataSource: DataSource) {
    Flyway.configure().dataSource(dataSource).load().migrate()
}

object Dependencies {
    val dataSource = HikariDataSource().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/real_world"
        username = "postgres"
        password = "postgres"
    }
    val database = Database.connect(dataSource)
    val userRepo: UserRepo = UserRepo(database)
    val userService: UserService = UserService(userRepo)
}


