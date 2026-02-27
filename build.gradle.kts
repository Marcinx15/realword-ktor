group = "com.example"
version = "0.0.1"

val kotlinVersion = "2.1.10"
val ktorVersion = "3.1.2"
val logbackVersion = "1.4.14"
val postgresVersion = "42.7.5"
val smiley4Version = "5.0.2"
val exposedVersion = "1.0.0-rc-4"
val jbcryptVersion = "0.4"
val flywayVersion = "12.0.2"


plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
}


application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")


    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("io.github.smiley4:ktor-openapi:$smiley4Version")
    implementation("io.github.smiley4:ktor-swagger-ui:$smiley4Version")

    implementation("org.mindrot:jbcrypt:${jbcryptVersion}")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

}
