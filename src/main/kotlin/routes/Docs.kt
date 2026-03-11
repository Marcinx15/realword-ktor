package com.example.routes

import com.example.routes.dto.LoginRequest
import com.example.routes.dto.RegisterUserRequest
import com.example.routes.dto.UserResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.ktor.http.HttpStatusCode

val registerUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Register user"
    request { body<RegisterUserRequest>() }
    response {
        HttpStatusCode.Created to {
            description = "Success"
            body<UserResponse> {}
        }
        HttpStatusCode.BadRequest to {
            description = "An invalid request"
        }
    }
}

val loginDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Login"
    request { body<LoginRequest>() }
    response {
        HttpStatusCode.OK to {
            description = "Success"
            body<UserResponse> {}
        }
    }
}

val getCurrentUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Get current user"
    response {
        HttpStatusCode.OK to {
            description = "Current user"
            body<UserResponse> {}
        }
    }
}

val updateUserDocs: RouteConfig.() -> Unit = {
    tags = listOf("user")
    description = "Update current user"
    response {
        HttpStatusCode.OK to {
            description = "Current user"
            body<UserResponse> {}
        }
    }
}
