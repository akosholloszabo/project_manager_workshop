package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.ProjectEditBuffer
import hu.akosholloszabo.project_manager.project_manager_workshop.model.ProjectsScreenState
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val projectStore: ProjectStore,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val scope = coroutineScope

    private val _selectedProjectPath = MutableStateFlow<String?>(null)
    private val _isEditing = MutableStateFlow(false)
    private val _editBuffer = MutableStateFlow(ProjectEditBuffer())

    val uiState: StateFlow<ProjectsScreenState> = combine(
        projectStore.projects,
        _selectedProjectPath,
        _isEditing,
        _editBuffer
    ) { projects, selectedPath, editing, buffer ->
        val selectedProject = selectedPath?.let { path ->
            projects.firstOrNull { it.file.canonicalPath == path }
        }
        ProjectsScreenState(
            projects = projects,
            selectedProjectPath = selectedPath,
            selectedProject = selectedProject,
            isEditing = editing,
            editBuffer = buffer
        )
    }.stateIn(scope, SharingStarted.Eagerly, ProjectsScreenState())

    init {
        scope.launch {
            projectStore.projects.collect { projects ->
                val nextPath = determineSelection(projects)
                if (nextPath != _selectedProjectPath.value) {
                    setSelectedProjectPathInternal(nextPath, projects, refreshBuffer = !_isEditing.value)
                }
            }
        }
    }

    fun refresh() {
        projectStore.refresh()
    }

    fun createProject() {
        scope.launch {
            val created = projectStore.createProject()
            if (created != null) {
                _selectedProjectPath.value = created.file.canonicalPath
                _editBuffer.value = editBufferFrom(created.value)
                _isEditing.value = true
            }
        }
    }

    fun startEditing() {
        val current = currentSelectedProject() ?: return
        _editBuffer.value = editBufferFrom(current.value)
        _isEditing.value = true
    }

    fun saveCurrentProject() {
        scope.launch {
            persistCurrentProject()
        }
    }

    fun deleteCurrentProject() {
        val current = currentSelectedProject() ?: return
        scope.launch {
            if (projectStore.deleteProject(current)) {
                _isEditing.value = false
                _editBuffer.value = ProjectEditBuffer()
                _selectedProjectPath.value = null
            }
        }
    }

    fun selectProject(path: String) {
        scope.launch {
            if (_isEditing.value) {
                persistCurrentProject()
            }
            setSelectedProjectPathInternal(path, projectStore.projects.value, refreshBuffer = !_isEditing.value)
        }
    }

    fun updateName(value: String) {
        _editBuffer.value = _editBuffer.value.copy(name = value)
    }

    fun updateDescription(value: String) {
        _editBuffer.value = _editBuffer.value.copy(description = value)
    }

    fun updateDetails(value: String) {
        _editBuffer.value = _editBuffer.value.copy(details = value)
    }

    private fun editBufferFrom(project: Project): ProjectEditBuffer {
        return ProjectEditBuffer(
            name = project.name,
            description = project.description,
            details = project.details
        )
    }

    private suspend fun persistCurrentProject(): Boolean {
        val current = currentSelectedProject() ?: return false
        val trimmedName = _editBuffer.value.name.trim().ifEmpty { current.value.name }
        val updatedProject = current.value.copy(
            name = trimmedName,
            description = _editBuffer.value.description,
            details = _editBuffer.value.details
        )
        val updatedPersisted = current.copy(value = updatedProject)
        val success = projectStore.saveProject(updatedPersisted, _editBuffer.value.details)
        if (success) {
            _isEditing.value = false
            _editBuffer.value = editBufferFrom(updatedProject)
        }
        return success
    }

    private fun currentSelectedProject(): Persisted<Project>? {
        val path = _selectedProjectPath.value ?: return null
        return projectStore.projects.value.firstOrNull { it.file.canonicalPath == path }
    }

    private fun determineSelection(projects: List<Persisted<Project>>): String? {
        val current = _selectedProjectPath.value
        return when {
            current != null && projects.any { it.file.canonicalPath == current } -> current
            projects.isNotEmpty() -> projects.first().file.canonicalPath
            else -> null
        }
    }

    private fun setSelectedProjectPathInternal(
        path: String?,
        projects: List<Persisted<Project>>,
        refreshBuffer: Boolean
    ) {
        _selectedProjectPath.value = path
        if (!_isEditing.value && refreshBuffer) {
            val selected = path?.let { target ->
                projects.firstOrNull { it.file.canonicalPath == target }
            }
            _editBuffer.value = selected?.let { editBufferFrom(it.value) } ?: ProjectEditBuffer()
        }
    }
}



