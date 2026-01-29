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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.model.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_delete
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_edit
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_save
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_empty_description
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_empty_details
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_empty_message
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_field_description
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_field_details
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_field_name
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_new
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.io.path.Path

@Composable
fun ProjectsScreen(projectsViewModel: ProjectsViewModel) {
    val selectedProjectPath by projectsViewModel.selectedProjectPath.collectAsStateWithLifecycle()
    val isEditing by projectsViewModel.isEditing.collectAsStateWithLifecycle()
    val editName by projectsViewModel.editName.collectAsStateWithLifecycle()
    val editDescription by projectsViewModel.editDescription.collectAsStateWithLifecycle()
    val editDetails by projectsViewModel.editDetails.collectAsStateWithLifecycle()
    val selectedProject by projectsViewModel.selectedProject.collectAsStateWithLifecycle()
    val projects by projectsViewModel.projects.collectAsStateWithLifecycle()

    LaunchedEffect(projectsViewModel) {
        projectsViewModel.refresh()
    }

    ProjectsScreenContent(
        selectedProjectPath = selectedProjectPath,
        projects = projects,
        selectedProject = selectedProject,
        isEditing = StateAndEvent(
            value = isEditing,
            event = { shouldEdit -> if (shouldEdit) projectsViewModel.startEditing() }
        ),
        onCreateProject = { projectsViewModel.createProject() },
        onSaveProject = { projectsViewModel.saveCurrentProject() },
        onDeleteProject = { projectsViewModel.deleteCurrentProject() },
        onSelectProject = projectsViewModel::selectProject,
        name = StateAndEvent(value = editName, event = projectsViewModel::updateName),
        description = StateAndEvent(value = editDescription, event = projectsViewModel::updateDescription),
        details = StateAndEvent(value = editDetails, event = projectsViewModel::updateDetails),
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ProjectsScreenContent(
    modifier: Modifier = Modifier,
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
    details: StateAndEvent<String>
) {
    Scaffold(modifier = modifier.padding(16.dp)) { _ ->
        Column() {
            Text(stringResource(Res.string.projects_title), style = MaterialTheme.typography.titleLarge)
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
                        Text(text = project.value.name, style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                },
                detail = {
                    DetailEditorPane(
                        modifier = Modifier.fillMaxSize(),
                        verticalSpacing = 12.dp,
                        header = {
                            DetailHeader(
                                title = selectedProject?.value?.name ?: stringResource(Res.string.projects_title),
                                actions = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        CrudActionBar(
                                            hasSelection = selectedProject != null,
                                            isEditing = isEditing.value,
                                            onNew = onCreateProject,
                                            onEdit = { isEditing.event(true) },
                                            onSave = onSaveProject,
                                            onDelete = onDeleteProject,
                                            labels = CrudActionLabels(
                                                newLabel = stringResource(Res.string.projects_new),
                                                editLabel = stringResource(Res.string.crud_edit),
                                                saveLabel = stringResource(Res.string.crud_save),
                                                deleteLabel = stringResource(Res.string.crud_delete)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            )
                        },
                        isEditing = isEditing.value,
                        editContent = {
                            Column(modifier = Modifier.fillMaxSize()) {
                                TextField(
                                    value = name.value,
                                    onValueChange = name.event,
                                    label = { Text(stringResource(Res.string.projects_field_name)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(8.dp))
                                TextField(
                                    value = description.value,
                                    onValueChange = description.event,
                                    label = { Text(stringResource(Res.string.projects_field_description)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    maxLines = Int.MAX_VALUE
                                )
                                Spacer(Modifier.height(8.dp))
                                TextField(
                                    value = details.value,
                                    onValueChange = details.event,
                                    label = { Text(stringResource(Res.string.projects_field_details)) },
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
                                    Text(text = project.value.name, style = MaterialTheme.typography.titleLarge)
                                    Spacer(Modifier.height(8.dp))
                                    Text(text = project.value.description, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(12.dp))
                                    SelectionContainer(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Markdown(
                                            project.value.details.ifBlank { stringResource(Res.string.projects_empty_details) }
                                        )
                                    }
                                }
                            } ?: EmptyDetailHint(
                                message = stringResource(Res.string.projects_empty_message),
                                description = stringResource(Res.string.projects_empty_description)
                            )
                        }
                    )
                }
            )
        }
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

    PreviewWrapper(darkTheme = false) {
        ProjectsScreenContent(
            projects = listOf(firstProject, secondProject),
            selectedProjectPath = firstProject.file.absolutePath,
            selectedProject = firstProject,
            isEditing = StateAndEvent(value = true, event = {}),
            name = StateAndEvent(value = firstProject.value.name, event = {}),
            description = StateAndEvent(value = firstProject.value.description, event = {}),
            details = StateAndEvent(value = firstProject.value.details, event = {}),
            onCreateProject = {},
            onSaveProject = {},
            onDeleteProject = {},
            onSelectProject = {},
        )
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
    PreviewWrapper(darkTheme = true) {
        ProjectsScreenContent(
            projects = listOf(firstProject, secondProject),
            selectedProjectPath = firstProject.file.absolutePath,
            selectedProject = firstProject,
            isEditing = StateAndEvent(value = true, event = {}),
            name = StateAndEvent(value = firstProject.value.name, event = {}),
            description = StateAndEvent(value = firstProject.value.description, event = {}),
            details = StateAndEvent(value = firstProject.value.details, event = {}),
            onCreateProject = {},
            onSaveProject = {},
            onDeleteProject = {},
            onSelectProject = {},
        )
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
