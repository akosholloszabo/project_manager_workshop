package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectStore(
    private val workingFolderStore: WorkingFolderStore?,
    private val projectsStorage: ProjectsStorage
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sessionValue: StorageSession? get() = workingFolderStore?.session?.value

    private val _projects = MutableStateFlow<List<Persisted<Project>>>(emptyList())
    val projects: StateFlow<List<Persisted<Project>>> = _projects.asStateFlow()

    init {
        scope.launch {
            val sessionFlow = workingFolderStore?.session
            if (sessionFlow == null) {
                refreshProjectsInternal(null)
            } else {
                sessionFlow.collectLatest { session ->
                    refreshProjectsInternal(session)
                }
            }
        }
    }

    // TODO why is these two functions both needed
    fun refreshProjects() {
        scope.launch {
            refreshProjectsInternal(sessionValue)
        }
    }

    private suspend fun refreshProjectsInternal(session: StorageSession?) {
        val loaded = withContext(Dispatchers.IO) {
            projectsStorage.loadProjects(session)
        }
        _projects.emit(loaded)
    }

    suspend fun createProject(name: String, description: String, details: String): Persisted<Project> {
        val created = withContext(Dispatchers.IO) {
            projectsStorage.createProject(sessionValue, name, description, details)
        }
        refreshProjects()
        return created
    }

    suspend fun saveProject(project: Persisted<Project>, details: String): Boolean {
        val success = withContext(Dispatchers.IO) {
            projectsStorage.saveProject(sessionValue, project.value, project.file, details)
        }
        if (success) {
            refreshProjects()
        }
        return success
    }

    suspend fun deleteProject(project: Persisted<Project>): Boolean {
        val success = withContext(Dispatchers.IO) {
            projectsStorage.deleteProject(sessionValue, project.file)
        }
        if (success) {
            refreshProjects()
        }
        return success
    }
}
