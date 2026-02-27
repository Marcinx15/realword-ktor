package com.example.persistence

import org.jetbrains.exposed.v1.core.dao.id.UIntIdTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserRepo(val db: Database) {
    fun createUser(username: String, email: String, passwordHash: String) {
        transaction(db) {
            UsersTable.insert {
                it[UsersTable.username] = username
                it[UsersTable.email] = email
                it[UsersTable.password] = passwordHash
            }
        }
    }
}

object UsersTable : UIntIdTable(name = "users") {
    val username = text(name = "username").uniqueIndex()
    val email = text(name = "email").uniqueIndex()
    val password = text(name = "password")
    val bio = text(name = "bio").nullable()
    val image = text(name= "image").nullable()
}