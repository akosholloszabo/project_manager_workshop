package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
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
    private val workingFolderStore: WorkingFolderStore,
    private val projectsStorage: ProjectsStorage
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _projects = MutableStateFlow<List<Persisted<Project>>>(emptyList())
    val projects: StateFlow<List<Persisted<Project>>> = _projects.asStateFlow()

    init {
        scope.launch {
            workingFolderStore.workingFolder.collectLatest { folder ->
                refreshProjectsInternal(folder)
            }
        }
    }

    fun refreshProjects() {
        scope.launch {
            refreshProjectsInternal(workingFolderStore.workingFolder.value)
        }
    }

    private suspend fun refreshProjectsInternal(folder: String?) {
        val loaded = withContext(Dispatchers.IO) {
            projectsStorage.loadProjects(folder)
        }
        _projects.emit(loaded)
    }

    suspend fun createProject(name: String? = null, description: String = ""): Persisted<Project>? {
        val created = withContext(Dispatchers.IO) {
            projectsStorage.createProject(workingFolderStore.workingFolder.value, name, description)
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
