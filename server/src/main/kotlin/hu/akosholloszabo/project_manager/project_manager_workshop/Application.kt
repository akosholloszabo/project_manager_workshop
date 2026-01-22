package hu.akosholloszabo.project_manager.project_manager_workshop

import hu.akosholloszabo.project_manager.project_manager_workshop.controller.noteRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.projectRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.ticketRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.db.DatabaseFactory
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NoteRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.TicketRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.service.NoteService
import hu.akosholloszabo.project_manager.project_manager_workshop.service.ProjectService
import hu.akosholloszabo.project_manager.project_manager_workshop.service.TicketService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module(config: ApplicationConfig = environment.config) {
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging)
    DatabaseFactory.init(config)

    val projectRepository = ProjectRepository()
    val projectService = ProjectService(projectRepository)
    val noteRepository = NoteRepository()
    val noteService = NoteService(noteRepository)
    val ticketRepository = TicketRepository()
    val ticketService = TicketService(ticketRepository)

    routing {
        projectRoutes(projectService)
        noteRoutes(noteService)
        ticketRoutes(ticketService)
    }
}
