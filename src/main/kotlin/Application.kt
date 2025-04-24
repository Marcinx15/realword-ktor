package com.example

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "openapi")
        get("/whatsup") {
            call.respondText("What's up motherfuckers?")
        }
    }
}
