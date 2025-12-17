package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProjectsScreenContent(workingFolder: String? = null) {
    val projectsState = remember { mutableStateListOf<ProjectsStorage.PersistedProject>() }
    var selectedProjectPath by rememberSaveable { mutableStateOf<String?>(null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editableName by rememberSaveable { mutableStateOf("") }
    var editableDescription by rememberSaveable { mutableStateOf("") }
    var editableDetails by rememberSaveable { mutableStateOf("") }

    fun refreshProjects(preservePath: String? = null) {
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
    }

    LaunchedEffect(workingFolder) {
        isEditing = false
        refreshProjects()
    }

    val selectedProject = selectedProjectPath?.let { path ->
        projectsState.find { it.file.canonicalPath == path }
    }

    LaunchedEffect(selectedProject?.file?.canonicalPath, isEditing) {
        if (!isEditing) {
            editableName = selectedProject?.project?.name ?: ""
            editableDescription = selectedProject?.project?.description ?: ""
            editableDetails = selectedProject?.project?.details ?: ""
        }
    }

    fun createNewProject() {
        val created = ProjectsStorage.createProject(workingFolder) ?: return
        isEditing = true
        refreshProjects(preservePath = created.file.canonicalPath)
        editableName = created.project.name
        editableDescription = created.project.description
        editableDetails = created.project.details
    }

    fun saveCurrentProject() {
        val current = selectedProject ?: return
        val trimmedName = editableName.trim()
        val updated = current.project.copy(
            name = trimmedName.ifEmpty { current.project.name },
            description = editableDescription,
            details = editableDetails
        )
        if (ProjectsStorage.saveProject(updated, current.file, editableDetails)) {
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Projects", style = MaterialTheme.typography.titleLarge)
        Text(
            workingFolder?.let { "Working folder: $it" } ?: "Working folder not set",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.width(320.dp).fillMaxHeight()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projectsState) { entry ->
                        val isSelected = entry.file.canonicalPath == selectedProjectPath
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable {
                                    if (isEditing) {
                                        saveCurrentProject()
                                    }
                                    isEditing = false
                                    selectedProjectPath = entry.file.canonicalPath
                                }
                                .padding(8.dp)
                        ) {
                            Text(entry.project.name, style = MaterialTheme.typography.titleMedium)
                            SimpleDivider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { createNewProject() }, enabled = workingFolder != null) {
                        Text("New project")
                    }
                    if (selectedProject != null) {
                        if (isEditing) {
                            Button(onClick = { saveCurrentProject() }) { Text("Save") }
                        } else {
                            Button(onClick = { isEditing = true }) { Text("Edit") }
                        }
                        Button(onClick = { deleteCurrentProject() }) { Text("Delete") }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (selectedProject == null) {
                    Text(
                        "Select a project to view or edit it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    if (isEditing) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextField(
                                value = editableName,
                                onValueChange = { editableName = it },
                                label = { Text("Project name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = editableDescription,
                                onValueChange = { editableDescription = it },
                                label = { Text("Description") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                maxLines = Int.MAX_VALUE
                            )
                            Spacer(Modifier.height(8.dp))
                            TextField(
                                value = editableDetails,
                                onValueChange = { editableDetails = it },
                                label = { Text("Details (Markdown)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                maxLines = Int.MAX_VALUE
                            )
                        }
                    } else {
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
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProjectsPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun ProjectsPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            ProjectsScreenContent()
        }
    }
}
