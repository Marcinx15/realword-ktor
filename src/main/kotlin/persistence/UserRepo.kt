package com.example.persistence

import com.example.model.Email
import com.example.model.User
import com.example.model.UserId
import com.example.model.Username
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserRepo(val db: Database) {

    fun createUser(username: String, email: String, passwordHash: String): UserId = transaction(db) {
        UsersTable.insertAndGetId {
            it[UsersTable.username] = username
            it[UsersTable.email] = email
            it[UsersTable.password] = passwordHash
        }
    }.let { UserId(it.value) }

    fun getUser(userId: UserId): User = transaction(db) {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq userId.value }
            .single()
            .toUser()
    }

    private fun ResultRow.toUser(): User = User(
        username = Username(get(UsersTable.username)),
        email = Email(get(UsersTable.email)),
        bio = get(UsersTable.bio),
        image = get(UsersTable.image),
    )
}


object UsersTable : IntIdTable(name = "users") {
    val username = text(name = "username").uniqueIndex()
    val email = text(name = "email").uniqueIndex()
    val password = text(name = "password")
    val bio = text(name = "bio").nullable()
    val image = text(name = "image").nullable()
}