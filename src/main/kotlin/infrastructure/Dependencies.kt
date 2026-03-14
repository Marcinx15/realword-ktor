package com.example.infrastructure

import com.example.persistence.UserRepo
import com.example.services.UserService
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import java.time.Clock

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
