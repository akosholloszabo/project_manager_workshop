package hu.akosholloszabo.project_manager.project_manager_workshop

import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NoteRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.TicketRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.service.NoteService
import hu.akosholloszabo.project_manager.project_manager_workshop.service.ProjectService
import hu.akosholloszabo.project_manager.project_manager_workshop.service.TicketService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val serverModule = module {
    singleOf(::ProjectRepository)
    singleOf(::ProjectService)
    singleOf(::NoteRepository)
    singleOf(::NoteService)
    singleOf(::TicketRepository)
    singleOf(::TicketService)
}
