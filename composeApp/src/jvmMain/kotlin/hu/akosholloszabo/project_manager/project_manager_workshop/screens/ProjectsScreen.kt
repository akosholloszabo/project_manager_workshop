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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import org.koin.core.context.GlobalContext.get
import org.koin.core.parameter.parametersOf
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.io.path.Path

@Composable
fun ProjectsScreen(workingFolder: String) {
    val projectsViewModel = remember(workingFolder) {
        get().get<ProjectsViewModel> { parametersOf(workingFolder) }
    }
    LaunchedEffect(workingFolder) {
        projectsViewModel.refresh()
    }

    val selectedProjectPath by projectsViewModel.selectedProjectPath.collectAsStateWithLifecycle()
    val isEditing by projectsViewModel.isEditing.collectAsStateWithLifecycle()
    val editName by projectsViewModel.editName.collectAsStateWithLifecycle()
    val editDescription by projectsViewModel.editDescription.collectAsStateWithLifecycle()
    val editDetails by projectsViewModel.editDetails.collectAsStateWithLifecycle()
    val selectedProject by projectsViewModel.selectedProject.collectAsStateWithLifecycle()
    val projects by projectsViewModel.projects.collectAsStateWithLifecycle()
    val isEditingState = StateAndEvent(
        state = isEditing,
        event = { shouldEdit -> if (shouldEdit) projectsViewModel.startEditing() }
    )
    val nameState = StateAndEvent(state = editName, event = projectsViewModel::updateName)
    val descriptionState = StateAndEvent(state = editDescription, event = projectsViewModel::updateDescription)
    val detailsState = StateAndEvent(state = editDetails, event = projectsViewModel::updateDetails)

    ProjectsScreenContent(
        selectedProjectPath = selectedProjectPath,
        projects = projects,
        selectedProject = selectedProject,
        isEditing = isEditingState,
        onCreateProject = projectsViewModel::createProject,
        onSaveProject = projectsViewModel::saveCurrentProject,
        onDeleteProject = projectsViewModel::deleteCurrentProject,
        onSelectProject = projectsViewModel::selectProject,
        name = nameState,
        description = descriptionState,
        details = detailsState,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ProjectsScreenContent(
    selectedProjectPath: String?,
    projects: List<Persisted<Project>>,
    selectedProject: Persisted<Project>?,
    isEditing: StateAndEvent<Boolean>,
    onCreateProject: () -> Unit,
    onSaveProject: () -> Unit,
    onDeleteProject: () -> Unit,
    onSelectProject: (String) -> Unit,
    name: StateAndEvent<String>,
    description: StateAndEvent<String>,
    details: StateAndEvent<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Projects", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            masterWeight = 0.33f,
            master = {
                SelectableList(
                    items = projects,
                    selectedKey = selectedProjectPath,
                    modifier = Modifier.fillMaxHeight(),
                    keyOf = { it.file.canonicalPath },
                    onItemClick = { entry -> onSelectProject(entry.file.canonicalPath) }
                ) { project, _ ->
                    Text(project.value.name, style = MaterialTheme.typography.titleMedium)
                    SimpleDivider(modifier = Modifier.padding(top = 8.dp))
                }
            },
            detail = {
                DetailEditorPane(
                    modifier = Modifier.fillMaxSize(),
                    verticalSpacing = 12.dp,
                    header = {
                        DetailHeader(
                            title = selectedProject?.value?.name ?: "Projects",
                            actions = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    CrudActionBar(
                                        hasSelection = selectedProject != null,
                                        isEditing = isEditing.state,
                                        onNew = onCreateProject,
                                        onEdit = { isEditing.event(true) },
                                        onSave = onSaveProject,
                                        onDelete = onDeleteProject,
                                        labels = CrudActionLabels(newLabel = "New project")
                                    )
                                }
                            }
                        )
                    },
                    isEditing = isEditing.state,
                    editContent = {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextField(
                                value = name.state,
                                onValueChange = name.event,
                                label = { Text("Project name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = description.state,
                                onValueChange = description.event,
                                label = { Text("Description") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                maxLines = Int.MAX_VALUE
                            )
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = details.state,
                                onValueChange = details.event,
                                label = { Text("Details (Markdown)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                maxLines = Int.MAX_VALUE
                            )
                        }
                    },
                    viewContent = {
                        selectedProject?.let { project ->
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(project.value.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    project.value.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(12.dp))
                                SelectionContainer(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Markdown(
                                        project.value.details.ifBlank { "*No details provided yet.*" }
                                    )
                                }
                            }
                        } ?: EmptyDetailHint(
                            message = "Select a project to view or edit it.",
                            description = "Choose from the list to the left"
                        )
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreviewLight() {
    val firstProject = previewPersistedProject(
        name = "alpha-project.json",
        id = 1,
        title = "Alpha Project",
        description = "First project preview",
        details = "# Overview\nPreview details"
    )
    val secondProject = previewPersistedProject(
        name = "beta-project.json",
        id = 2,
        title = "Beta Project",
        description = "Second project preview",
        details = "# Status\nMore preview content"
    )

    AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent(
                projects = listOf(firstProject, secondProject),
                selectedProjectPath = firstProject.file.absolutePath,
                selectedProject = firstProject,
                isEditing = StateAndEvent(state = true, event = {}),
                name = StateAndEvent(state = firstProject.value.name, event = {}),
                description = StateAndEvent(state = firstProject.value.description, event = {}),
                details = StateAndEvent(state = firstProject.value.details, event = {}),
                onCreateProject = {},
                onSaveProject = {},
                onDeleteProject = {},
                onSelectProject = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProjectsScreenPreviewDark() {
    val firstProject = previewPersistedProject(
        name = "alpha-project.json",
        id = 1,
        title = "Alpha Project",
        description = "First project preview",
        details = "# Overview\nPreview details"
    )
    val secondProject = previewPersistedProject(
        name = "beta-project.json",
        id = 2,
        title = "Beta Project",
        description = "Second project preview",
        details = "# Status\nMore preview content"
    )

    AppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent(
                projects = listOf(firstProject, secondProject),
                selectedProjectPath = firstProject.file.absolutePath,
                selectedProject = firstProject,
                isEditing = StateAndEvent(state = true, event = {}),
                name = StateAndEvent(state = firstProject.value.name, event = {}),
                description = StateAndEvent(state = firstProject.value.description, event = {}),
                details = StateAndEvent(state = firstProject.value.details, event = {}),
                onCreateProject = {},
                onSaveProject = {},
                onDeleteProject = {},
                onSelectProject = {},
            )
        }
    }
}

private fun previewPersistedProject(
    name: String,
    id: Int,
    title: String,
    description: String,
    details: String
): Persisted<Project> {
    val file = Path(name).toFile().absoluteFile
    return Persisted(file, Project(id, title, description, details))
}
