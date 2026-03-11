package com.example.infrastructure

import org.flywaydb.core.Flyway
import javax.sql.DataSource

fun runDbMigrations(dataSource: DataSource) {
    Flyway.configure().dataSource(dataSource).load().migrate()
}