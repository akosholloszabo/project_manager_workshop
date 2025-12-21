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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProjectsScreenContent(workingFolder: String) {
    val projectsViewModel = remember(workingFolder) {
        ProjectsViewModel(ProjectStore(workingFolder))
    }
    LaunchedEffect(workingFolder) {
        projectsViewModel.refresh()
    }
    val uiState by projectsViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Projects", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            masterWeight = 0.33f,
            master = {
                SelectableList(
                    items = uiState.projects,
                    selectedKey = uiState.selectedProjectPath,
                    modifier = Modifier.fillMaxHeight(),
                    keyOf = { it.file.canonicalPath },
                    onItemClick = { entry: Persisted<Project> ->
                        projectsViewModel.selectProject(entry.file.canonicalPath)
                    }
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
                            title = uiState.selectedProject?.value?.name ?: "Projects",
                            actions = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    CrudActionBar(
                                        hasSelection = uiState.selectedProject != null,
                                        isEditing = uiState.isEditing,
                                        onNew = { projectsViewModel.createProject() },
                                        onEdit = { projectsViewModel.startEditing() },
                                        onSave = { projectsViewModel.saveCurrentProject() },
                                        onDelete = { projectsViewModel.deleteCurrentProject() },
                                        labels = CrudActionLabels(newLabel = "New project")
                                    )
                                }
                            }
                        )
                    },
                    isEditing = uiState.isEditing,
                    editContent = {
                        val editorKey = "project-${uiState.selectedProjectPath ?: "unselected"}"
                        val nameKey = "$editorKey-name"
                        val descriptionKey = "$editorKey-description"
                        val detailsKey = "$editorKey-details"
                        var draftName by rememberSaveable(nameKey) { mutableStateOf(uiState.editBuffer.name) }
                        var draftDescription by rememberSaveable(descriptionKey) { mutableStateOf(uiState.editBuffer.description) }
                        var draftDetails by rememberSaveable(detailsKey) { mutableStateOf(uiState.editBuffer.details) }
                        LaunchedEffect(nameKey, uiState.editBuffer.name) {
                            if (draftName != uiState.editBuffer.name) {
                                draftName = uiState.editBuffer.name
                            }
                        }
                        LaunchedEffect(descriptionKey, uiState.editBuffer.description) {
                            if (draftDescription != uiState.editBuffer.description) {
                                draftDescription = uiState.editBuffer.description
                            }
                        }
                        LaunchedEffect(detailsKey, uiState.editBuffer.details) {
                            if (draftDetails != uiState.editBuffer.details) {
                                draftDetails = uiState.editBuffer.details
                            }
                        }
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextField(
                                value = draftName,
                                onValueChange = {
                                    draftName = it
                                    projectsViewModel.updateName(it)
                                },
                                label = { Text("Project name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = draftDescription,
                                onValueChange = {
                                    draftDescription = it
                                    projectsViewModel.updateDescription(it)
                                },
                                label = { Text("Description") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                maxLines = Int.MAX_VALUE
                            )
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = draftDetails,
                                onValueChange = {
                                    draftDetails = it
                                    projectsViewModel.updateDetails(it)
                                },
                                label = { Text("Details (Markdown)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                maxLines = Int.MAX_VALUE
                            )
                        }
                    },
                    viewContent = {
                        if (uiState.selectedProject != null) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(uiState.selectedProject!!.value.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    uiState.selectedProject!!.value.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(12.dp))
                                SelectionContainer(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Markdown(
                                        uiState.selectedProject!!.value.details.ifBlank { "*No details provided yet.*" }
                                    )
                                }
                            }
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
