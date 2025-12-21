package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectStore(
    private val workingFolder: String?,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = coroutineScope

    private val _projects = MutableStateFlow<List<Persisted<Project>>>(emptyList())
    val projects: StateFlow<List<Persisted<Project>>> = _projects.asStateFlow()

    init {
        scope.launch { refreshProjects() }
    }

    fun refresh() {
        scope.launch { refreshProjects() }
    }

    private suspend fun refreshProjects() {
        val loaded = withContext(ioDispatcher) {
            ProjectsStorage.loadProjects(workingFolder)
        }
        _projects.value = loaded
    }

    suspend fun createProject(name: String? = null, description: String = ""): Persisted<Project>? {
        val created = withContext(ioDispatcher) {
            ProjectsStorage.createProject(workingFolder, name, description)
        }
        if (created != null) {
            refreshProjects()
        }
        return created
    }

    suspend fun saveProject(project: Persisted<Project>, details: String): Boolean {
        val success = withContext(ioDispatcher) {
            ProjectsStorage.saveProject(project.value, project.file, details)
        }
        if (success) {
            refreshProjects()
        }
        return success
    }

    suspend fun deleteProject(project: Persisted<Project>): Boolean {
        val success = withContext(ioDispatcher) {
            ProjectsStorage.deleteProject(project.file)
        }
        if (success) {
            refreshProjects()
        }
        return success
    }
}

