package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val projectStore: ProjectStore
) : ViewModel() {
    private val _selectedProjectPath = MutableStateFlow<String?>(null)
    val selectedProjectPath: StateFlow<String?> = _selectedProjectPath.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editName = MutableStateFlow("")
    val editName: StateFlow<String> = _editName.asStateFlow()
    private val _editDescription = MutableStateFlow("")
    val editDescription: StateFlow<String> = _editDescription.asStateFlow()
    private val _editDetails = MutableStateFlow("")
    val editDetails: StateFlow<String> = _editDetails.asStateFlow()

    val projects: StateFlow<List<Persisted<Project>>> = projectStore.projects

    val selectedProject: StateFlow<Persisted<Project>?> = combine(
        projectStore.projects,
        _selectedProjectPath
    ) { projects, path ->
        projects.findByPath(path)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            projectStore.projects.collect { projects ->
                val nextPath = determineSelection(projects)
                if (nextPath != selectedProjectPath.value) {
                    setSelectedProjectPathInternal(
                        path = nextPath,
                        projects = projects,
                        refreshBuffer = !isEditing.value
                    )
                }
            }
        }
    }

    fun refresh() = projectStore.refreshProjects()

    fun createProject() {
        projectStore.createProject(
            name = "",
            description = "",
            details = ""
        )?.let { created ->
            _selectedProjectPath.tryEmit(created.file.canonicalPath)
            _editName.tryEmit(created.value.name)
            _editDescription.tryEmit(created.value.description)
            _editDetails.tryEmit(created.value.details)
            _isEditing.tryEmit(true)
        }
    }

    fun startEditing() {
        val current = currentSelectedProject() ?: return
        _editName.tryEmit(current.value.name)
        _editDescription.tryEmit(current.value.description)
        _editDetails.tryEmit(current.value.details)
        _isEditing.tryEmit(true)
    }

    fun saveCurrentProject() {
        if (!isEditing.value) return
        persistCurrentProject()
    }

    fun deleteCurrentProject() {
        val current = currentSelectedProject() ?: return
        if (projectStore.deleteProject(current)) {
            _isEditing.tryEmit(false)
            _editName.tryEmit("")
            _editDescription.tryEmit("")
            _editDetails.tryEmit("")
            _selectedProjectPath.tryEmit(null)
        }
    }

    fun selectProject(path: String) {
        viewModelScope.launch {
            if (selectedProjectPath.value == path) return@launch
            if (!saveIfEditing()) return@launch
            setSelectedProjectPathInternal(path = path, projects = projectStore.projects.value, refreshBuffer = true)
        }
    }

    fun updateName(value: String) = _editName.tryEmit(value)

    fun updateDescription(value: String) = _editDescription.tryEmit(value)

    fun updateDetails(value: String) = _editDetails.tryEmit(value)

    private fun persistCurrentProject(): Boolean {
        val current = currentSelectedProject() ?: return false
        val trimmedName = editName.value.trim().ifEmpty { current.value.name }
        val updatedProject = current.value.copy(
            name = trimmedName,
            description = editDescription.value,
            details = editDetails.value
        )
        val updatedPersisted = current.copy(value = updatedProject)
        val success = projectStore.saveProject(updatedPersisted, editDetails.value)
        if (success) {
            _editName.tryEmit(updatedProject.name)
            _editDescription.tryEmit(updatedProject.description)
            _editDetails.tryEmit(updatedProject.details)
            _isEditing.tryEmit(false)
        }
        return success
    }

    private fun saveIfEditing(): Boolean =
        if (!isEditing.value) true else persistCurrentProject()

    private fun currentSelectedProject(): Persisted<Project>? =
        selectedProjectPath.value?.let { path ->
            projectStore.projects.value.firstOrNull { it.file.canonicalPath == path }
        }

    private fun determineSelection(projects: List<Persisted<Project>>): String? = when {
        selectedProjectPath.value?.let { current -> projects.any { it.file.canonicalPath == current } } == true -> selectedProjectPath.value
        projects.isNotEmpty() -> projects.first().file.canonicalPath
        else -> null
    }

    private fun setSelectedProjectPathInternal(
        path: String?,
        projects: List<Persisted<Project>>,
        refreshBuffer: Boolean
    ) {
        _selectedProjectPath.tryEmit(path)
        if (!isEditing.value && refreshBuffer) {
            val selected = path?.let { target ->
                projects.firstOrNull { it.file.canonicalPath == target }
            }
            selected?.let {
                _editName.tryEmit(it.value.name)
                _editDescription.tryEmit(it.value.description)
                _editDetails.tryEmit(it.value.details)
            } ?: run {
                _editName.tryEmit("")
                _editDescription.tryEmit("")
                _editDetails.tryEmit("")
            }
        }
    }

    private fun List<Persisted<Project>>.findByPath(path: String?): Persisted<Project>? =
        path?.let { requested ->
            firstOrNull { it.file.canonicalPath == requested }
        }
}
