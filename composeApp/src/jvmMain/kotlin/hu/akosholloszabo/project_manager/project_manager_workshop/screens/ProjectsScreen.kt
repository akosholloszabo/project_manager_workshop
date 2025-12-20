package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProjectsScreenContent(workingFolder: String) {
    val controller = rememberProjectsController(workingFolder)
    val projects = controller.projects
    val selectedProject = controller.selectedProject
    val selectedPath = controller.selectedProjectPath

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Projects", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            masterWeight = 0.33f,
            master = {
                ProjectListPane(
                    projects = projects,
                    selectedPath = selectedPath,
                    onProjectSelected = controller::selectProject,
                    modifier = Modifier.fillMaxHeight()
                )
            },
            detail = {
                DetailEditorPane(
                    modifier = Modifier.fillMaxSize(),
                    verticalSpacing = 12.dp,
                    header = {
                        DetailHeader(
                            title = selectedProject?.project?.name ?: "Projects",
                            actions = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    CrudActionBar(
                                        hasSelection = selectedProject != null,
                                        isEditing = controller.isEditing,
                                        onNew = controller::createProject,
                                        onEdit = controller::startEditing,
                                        onSave = controller::saveCurrentProject,
                                        onDelete = controller::deleteCurrentProject,
                                        labels = CrudActionLabels(newLabel = "New project")
                                    )
                                }
                            }
                        )
                    },
                    isEditing = controller.isEditing,
                    editContent = {
                        ProjectEditor(
                            buffer = controller.editBuffer,
                            onNameChange = controller::updateName,
                            onDescriptionChange = controller::updateDescription,
                            onDetailsChange = controller::updateDetails
                        )
                    },
                    viewContent = {
                        if (selectedProject != null) {
                            ProjectViewer(selectedProject)
                        } else {
                            EmptyDetailHint(
                                message = "Select a project to view or edit it.",
                                description = "Choose from the list to the left"
                            )
                        }
                    }
                )
            }
        )
    }
}

@Composable
private fun ProjectListPane(
    projects: List<ProjectsStorage.PersistedProject>,
    selectedPath: String?,
    onProjectSelected: (ProjectsStorage.PersistedProject) -> Unit,
    modifier: Modifier = Modifier
) {
    SelectableList(
        items = projects,
        selectedKey = selectedPath,
        modifier = modifier,
        keyOf = { it.file.canonicalPath },
        onItemClick = onProjectSelected
    ) { project, _ ->
        Text(project.project.name, style = MaterialTheme.typography.titleMedium)
        SimpleDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun ProjectEditor(
    buffer: ProjectEditBuffer,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = buffer.name,
            onValueChange = onNameChange,
            label = { Text("Project name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = buffer.description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = Int.MAX_VALUE
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = buffer.details,
            onValueChange = onDetailsChange,
            label = { Text("Details (Markdown)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            maxLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun ProjectViewer(selectedProject: ProjectsStorage.PersistedProject) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(selectedProject.project.name, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(selectedProject.project.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        SelectionContainer(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Markdown(
                selectedProject.project.details.ifBlank { "*No details provided yet.*" }
            )
        }
    }
}

private data class ProjectEditBuffer(
    val name: String = "",
    val description: String = "",
    val details: String = ""
)

@Composable
private fun rememberProjectsController(workingFolder: String): ProjectsController {
    val controller = remember(workingFolder) { ProjectsController(workingFolder) }
    LaunchedEffect(controller, workingFolder) {
        controller.loadInitial()
    }
    return controller
}

private class ProjectsController(private val workingFolder: String) {
    private val projectsState = mutableStateListOf<ProjectsStorage.PersistedProject>()

    val projects: List<ProjectsStorage.PersistedProject>
        get() = projectsState

    var selectedProjectPath by mutableStateOf<String?>(null)
        private set

    var isEditing by mutableStateOf(false)
        private set

    var editBuffer by mutableStateOf(ProjectEditBuffer())
        private set

    val selectedProject: ProjectsStorage.PersistedProject?
        get() = selectedProjectPath?.let { path ->
            projectsState.firstOrNull { it.file.canonicalPath == path }
        }

    fun loadInitial() {
        isEditing = false
        refreshProjects()
    }

    fun createProject() {
        val created = ProjectsStorage.createProject(workingFolder) ?: return
        refreshProjects(preservePath = created.file.canonicalPath)
        isEditing = true
        editBuffer = ProjectEditBuffer(
            name = created.project.name,
            description = created.project.description,
            details = created.project.details
        )
    }

    fun startEditing() {
        val current = selectedProject ?: return
        isEditing = true
        editBuffer = ProjectEditBuffer(
            name = current.project.name,
            description = current.project.description,
            details = current.project.details
        )
    }

    fun saveCurrentProject() {
        val current = selectedProject ?: return
        val trimmedName = editBuffer.name.trim().ifEmpty { current.project.name }
        val updated = current.project.copy(
            name = trimmedName,
            description = editBuffer.description,
            details = editBuffer.details
        )
        if (ProjectsStorage.saveProject(updated, current.file, editBuffer.details)) {
            isEditing = false
            refreshProjects(preservePath = current.file.canonicalPath)
        }
    }

    fun deleteCurrentProject() {
        val current = selectedProject ?: return
        if (ProjectsStorage.deleteProject(current.file)) {
            isEditing = false
            refreshProjects()
        }
    }

    fun selectProject(entry: ProjectsStorage.PersistedProject) {
        if (isEditing && selectedProject != null) {
            saveCurrentProject()
        }
        isEditing = false
        selectedProjectPath = entry.file.canonicalPath
        syncEditBufferWithSelection()
    }

    fun updateName(value: String) {
        editBuffer = editBuffer.copy(name = value)
    }

    fun updateDescription(value: String) {
        editBuffer = editBuffer.copy(description = value)
    }

    fun updateDetails(value: String) {
        editBuffer = editBuffer.copy(details = value)
    }

    private fun refreshProjects(preservePath: String? = null) {
        val loaded = ProjectsStorage.loadProjects(workingFolder)
        projectsState.apply {
            clear()
            addAll(loaded)
        }
        selectedProjectPath = when {
            preservePath != null && loaded.any { it.file.canonicalPath == preservePath } -> preservePath
            loaded.isNotEmpty() -> loaded[0].file.canonicalPath
            else -> null
        }
        if (!isEditing) {
            syncEditBufferWithSelection()
        }
    }

    private fun syncEditBufferWithSelection() {
        val current = selectedProject
        editBuffer = if (current != null) {
            ProjectEditBuffer(
                name = current.project.name,
                description = current.project.description,
                details = current.project.details
            )
        } else {
            ProjectEditBuffer()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreviewLight() {
    AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent(workingFolder = "preview")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent(workingFolder = "preview")
        }
    }
}
