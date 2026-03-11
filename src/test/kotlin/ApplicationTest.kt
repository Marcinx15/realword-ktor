package com.example

import com.example.infrastructure.configure
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            configure()
        }
        client.get("/docs").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
