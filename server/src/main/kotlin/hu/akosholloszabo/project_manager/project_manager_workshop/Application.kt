package hu.akosholloszabo.project_manager.project_manager_workshop

import com.typesafe.config.ConfigFactory
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.noteRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.projectRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.ticketRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.db.DatabaseFactory
import hu.akosholloszabo.project_manager.project_manager_workshop.di.serverModule
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin

fun main() {
    val runtimeConfig = HoconApplicationConfig(ConfigFactory.load())
    embeddedServer(Netty, host = "0.0.0.0", port = SERVER_PORT) {
        module(runtimeConfig)
    }.start(wait = true)
}

fun Application.module(config: ApplicationConfig = environment.config) {
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging)
    DatabaseFactory.init(config)
    install(Koin) {
        modules(serverModule)
    }
    routing {
        projectRoutes(get())
        noteRoutes(get())
        ticketRoutes(get())
    }
}
