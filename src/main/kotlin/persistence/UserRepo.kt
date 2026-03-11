package com.example.persistence

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import com.example.model.Email
import com.example.model.EmailAlreadyTaken
import com.example.model.User
import com.example.model.UserError
import com.example.model.UserId
import com.example.model.Username
import com.example.model.UsernameAlreadyTaken
import com.example.routes.dto.FieldUpdate
import com.example.services.PlainPassword

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.updateReturning
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState

@JvmInline
value class HashedPassword(val value: String)
data class UserRecord(val userId: UserId, val user: User, val password: HashedPassword)

class UserRepo(val db: Database) {

    fun createUser(
        username: Username,
        email: Email,
        passwordHash: HashedPassword
    ): Either<UserError, UserId> = either {
        catch(
            block = {
                transaction(db) {
                    UsersTable.insertAndGetId {
                        it[UsersTable.username] = username.value
                        it[UsersTable.email] = email.value
                        it[UsersTable.password] = passwordHash.value
                    }.let { UserId(it.value) }
                }
            },
            catch = { ex: ExposedSQLException ->
                if (ex.sqlState == PSQLState.UNIQUE_VIOLATION.state) {
                    when ((ex.cause as? PSQLException)?.serverErrorMessage?.constraint) {
                        "users_email_key" -> raise(EmailAlreadyTaken)
                        "users_username_key" -> raise(UsernameAlreadyTaken)
                        else -> throw ex
                    }
                } else throw ex
            }
        )
    }

    fun updateUser(
        userId: UserId,
        username: FieldUpdate<Username>,
        email: FieldUpdate<Email>,
        password: FieldUpdate<PlainPassword>,
        bio: FieldUpdate<String?>,
        image: FieldUpdate<String?>,
    ): Either<UserError, User?> = either {
        catch(
            block = {
                transaction(db) {
                    UsersTable.updateReturning(where = { UsersTable.id eq userId.value }) { statement ->
                        username.forEach { statement[UsersTable.username] = it.value }
                        email.forEach { statement[UsersTable.email] = it.value }
                        password.forEach { statement[UsersTable.password] = it.value }
                        bio.forEach { statement[UsersTable.bio] = it }
                        image.forEach { statement[UsersTable.image] = it }
                    }.singleOrNull()?.toUser()
                }
            },
            catch = { ex: ExposedSQLException ->
                if (ex.sqlState == PSQLState.UNIQUE_VIOLATION.state) {
                    when ((ex.cause as? PSQLException)?.serverErrorMessage?.constraint) {
                        "users_email_key" -> raise(EmailAlreadyTaken)
                        "users_username_key" -> raise(UsernameAlreadyTaken)
                        else -> throw ex
                    }
                } else throw ex
            }
        )
    }

    fun getUserById(userId: UserId): User? = transaction(db) {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq userId.value }
            .singleOrNull()
            ?.toUser()
    }

    fun getUserByEmailForAuth(email: Email): UserRecord? = transaction(db) {
        UsersTable
            .selectAll()
            .where { UsersTable.email eq email.value }
            .singleOrNull()
            ?.let {
                UserRecord(
                    userId = UserId(it[UsersTable.id].value),
                    user = it.toUser(),
                    password = HashedPassword(it[UsersTable.password])
                )
            }
    }

    private fun ResultRow.toUser(): User = User(
        username = Username.fromDb(get(UsersTable.username)),
        email = Email.fromDb(get(UsersTable.email)),
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