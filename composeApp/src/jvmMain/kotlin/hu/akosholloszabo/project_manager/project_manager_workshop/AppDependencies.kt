package hu.akosholloszabo.project_manager.project_manager_workshop

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel

/**
 * Aggregates all dependencies the UI needs. Built manually by [AppBootstrap].
 */
data class AppDependencies(
    val storageBackend: StorageBackend,
    val notesViewModel: NotesViewModel,
    val projectsViewModel: ProjectsViewModel,
    val ticketsViewModel: TicketsViewModel,
    val workingFolderViewModel: WorkingFolderViewModel?,
)
