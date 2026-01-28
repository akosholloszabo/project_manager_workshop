package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProjectStore(
    private val workingFolderStore: WorkingFolderStore?,
    private val projectsStorage: ProjectsStorage
) {
    private val _projects = MutableStateFlow<List<Persisted<Project>>>(emptyList())
    val projects: StateFlow<List<Persisted<Project>>> = _projects.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            workingFolderStore?.session?.let { sessionFlow ->
                sessionFlow.collectLatest { session ->
                    refreshProjects(session)
                }
            } ?: refreshProjects(null)
        }
    }

    fun refreshProjects(session: StorageSession? = workingFolderStore?.session?.value) =
        _projects.tryEmit(projectsStorage.loadProjects(session))

    fun createProject(name: String, description: String, details: String): Persisted<Project>? =
        projectsStorage.createProject(
            workingFolderStore?.session?.value,
            name,
            description,
            details
        )?.also {
            refreshProjects()
        }

    fun saveProject(project: Persisted<Project>, details: String): Boolean =
        projectsStorage.saveProject(
            workingFolderStore?.session?.value,
            project.value,
            project.file,
            details
        )
            .also { success ->
                if (success) {
                    refreshProjects()
                }
            }

    fun deleteProject(project: Persisted<Project>): Boolean =
        projectsStorage.deleteProject(
            workingFolderStore?.session?.value,
            project.file
        ).also { success ->
            if (success) {
                refreshProjects()
            }
        }
}
