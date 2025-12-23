package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NoteRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NoteRepositoryImpl
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectRepositoryImpl
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.TicketRepository
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.TicketRepositoryImpl
import hu.akosholloszabo.project_manager.project_manager_workshop.service.NoteService
import hu.akosholloszabo.project_manager.project_manager_workshop.service.NoteServiceImpl
import hu.akosholloszabo.project_manager.project_manager_workshop.service.ProjectService
import hu.akosholloszabo.project_manager.project_manager_workshop.service.ProjectServiceImpl
import hu.akosholloszabo.project_manager.project_manager_workshop.service.TicketService
import hu.akosholloszabo.project_manager.project_manager_workshop.service.TicketServiceImpl
import org.koin.dsl.module

val serverModule = module {
    single<ProjectRepository> { ProjectRepositoryImpl() }
    single<ProjectService> { ProjectServiceImpl(get()) }
    single<NoteRepository> { NoteRepositoryImpl() }
    single<NoteService> { NoteServiceImpl(get()) }
    single<TicketRepository> { TicketRepositoryImpl() }
    single<TicketService> { TicketServiceImpl(get()) }
}
