package hu.akosholloszabo.project_manager.project_manager_workshop.model

data class ProjectsScreenState(
    val projects: List<Persisted<Project>> = emptyList(),
    val selectedProjectPath: String? = null,
    val selectedProject: Persisted<Project>? = null,
    val isEditing: Boolean = false,
    val editBuffer: ProjectEditBuffer = ProjectEditBuffer()
)
