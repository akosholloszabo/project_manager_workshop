package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProjectStore(
    private val workingFolder: String?,
    private val projectsStorage: ProjectsStorage
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _projects = MutableStateFlow<List<Persisted<Project>>>(emptyList())
    val projects: StateFlow<List<Persisted<Project>>> = _projects.asStateFlow()

    init {
        refreshProjects()
    }

    fun refreshProjects() {
        scope.launch {
            val loaded = projectsStorage.loadProjects(workingFolder)
            _projects.emit(loaded)
        }
    }

    suspend fun createProject(name: String? = null, description: String = ""): Persisted<Project>? {
        val created = withContext(Dispatchers.IO) {
            projectsStorage.createProject(workingFolder, name, description)
        }
        if (created != null) {
            refreshProjects()
        }
        return created
    }

    suspend fun saveProject(project: Persisted<Project>, details: String): Boolean {
        val success = withContext(Dispatchers.IO) {
            projectsStorage.saveProject(project.value, project.file, details)
        }
        if (success) {
            refreshProjects()
        }
        return success
    }

    suspend fun deleteProject(project: Persisted<Project>): Boolean {
        val success = withContext(Dispatchers.IO) {
            projectsStorage.deleteProject(project.file)
        }
        if (success) {
            refreshProjects()
        }
        return success
    }
}
