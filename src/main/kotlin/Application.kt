package com.example

import com.example.infrastructure.Dependencies
import com.example.infrastructure.configure
import com.example.infrastructure.runDbMigrations
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    runDbMigrations(Dependencies.dataSource)
    val ktorConfig = Dependencies.config.ktor
    embeddedServer(Netty, port = ktorConfig.port, host = ktorConfig.host) {
        configure()
    }.start(wait = true)
}
