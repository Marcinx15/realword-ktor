group = "com.example"
version = "0.0.1"

val kotlinVersion = "2.3.10"
val ktorVersion = "3.1.2"
val postgresVersion = "42.7.5"
val smiley4Version = "5.0.2"
val exposedVersion = "1.0.0-rc-4"
val jbcryptVersion = "0.4"
val flywayVersion = "12.0.2"
val kotlinxSerializationVersion = "1.10.0"
val arrowVersion = "2.2.1.1"
val kotlinLoggingVersion = "8.0.01"
val logbackVersion = "1.5.32"


plugins {
    kotlin("jvm") version "2.3.10"
    id("io.ktor.plugin") version "3.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.10"
}


application {
    mainClass = "com.example.ApplicationKt"
}

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

tasks.test {
    environment["JWT_SECRET"] = "jwt-secret-key"
    environment["POSTGRES_USER_PASSWORD"] = "postgres-user-password"
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-config-yaml")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-status-pages")
    testImplementation("io.ktor:ktor-server-test-host")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:$kotlinxSerializationVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("io.arrow-kt:arrow-core:$arrowVersion")



    implementation("io.github.smiley4:ktor-openapi:$smiley4Version")
    implementation("io.github.smiley4:ktor-swagger-ui:$smiley4Version")

    implementation("org.mindrot:jbcrypt:${jbcryptVersion}")

    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation("io.github.oshai:kotlin-logging:${kotlinLoggingVersion}")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

}
