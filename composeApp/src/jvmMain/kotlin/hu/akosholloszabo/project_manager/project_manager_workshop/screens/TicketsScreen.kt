package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

private fun TicketStatus.icon(): ImageVector = when (this) {
    TicketStatus.Backlog -> Icons.Default.Schedule
    TicketStatus.ReadyForRefinement -> Icons.Default.Lightbulb
    TicketStatus.InRefinement -> Icons.Default.Autorenew
    TicketStatus.ReadyForProcessing -> Icons.Default.PlayCircle
    TicketStatus.InProcessing -> Icons.Default.Build
    TicketStatus.ReadyForTesting -> Icons.Default.BugReport
    TicketStatus.InTesting -> Icons.Default.Help
    TicketStatus.Completed -> Icons.Default.CheckCircle
    TicketStatus.Rejected -> Icons.Default.Cancel
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreenContent(workingFolder: String? = null) {
    val projectsState = remember { mutableStateListOf<ProjectsStorage.PersistedProject>() }
    val ticketsState = remember { mutableStateListOf<TicketsStorage.PersistedTicket>() }
    var selectedTicketPath by rememberSaveable { mutableStateOf<String?>(null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editableTitle by rememberSaveable { mutableStateOf("") }
    var editableProjectId by rememberSaveable { mutableStateOf<Int?>(null) }
    var editableStatus by rememberSaveable { mutableStateOf(TicketStatus.default.displayText) }
    var editableDetails by rememberSaveable { mutableStateOf("") }
    var statusDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var projectDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    fun refreshProjects() {
        val loaded = ProjectsStorage.loadProjects(workingFolder)
        projectsState.apply {
            clear()
            addAll(loaded)
        }
    }

    fun refreshTickets(preservePath: String? = null) {
        val loaded = TicketsStorage.loadTickets(workingFolder)
        ticketsState.apply {
            clear()
            addAll(loaded)
        }
        selectedTicketPath = when {
            preservePath != null && loaded.any { it.file.canonicalPath == preservePath } -> preservePath
            loaded.isNotEmpty() -> loaded[0].file.canonicalPath
            else -> null
        }
    }

    LaunchedEffect(workingFolder) {
        isEditing = false
        refreshProjects()
        refreshTickets()
    }

    val selectedTicket = selectedTicketPath?.let { path ->
        ticketsState.find { it.file.canonicalPath == path }
    }

    LaunchedEffect(selectedTicket?.file?.canonicalPath, isEditing) {
        if (!isEditing) {
            editableTitle = selectedTicket?.ticket?.title ?: ""
            editableProjectId = selectedTicket?.ticket?.projectId ?: projectsState.firstOrNull()?.project?.id
            editableStatus = selectedTicket?.ticket?.status?.displayText ?: TicketStatus.default.displayText
            editableDetails = selectedTicket?.ticket?.details ?: ""
        }
    }

    fun createNewTicket() {
        val created = TicketsStorage.createTicket(workingFolder) ?: return
        isEditing = true
        refreshTickets(preservePath = created.file.canonicalPath)
        editableTitle = created.ticket.title
        editableProjectId = created.ticket.projectId
        editableStatus = created.ticket.status.displayText
        editableDetails = created.ticket.details
    }

    fun saveCurrentTicket() {
        val current = selectedTicket ?: return
        val projectIdValue = editableProjectId ?: current.ticket.projectId
        val updated = current.ticket.copy(
            title = editableTitle.trim().ifEmpty { current.ticket.title },
            projectId = projectIdValue,
            status = TicketStatus.fromDisplay(editableStatus),
            details = editableDetails
        )
        if (TicketsStorage.saveTicket(updated, current.file, editableDetails)) {
            isEditing = false
            refreshTickets(preservePath = current.file.canonicalPath)
        }
    }

    fun deleteCurrentTicket() {
        val current = selectedTicket ?: return
        if (TicketsStorage.deleteTicket(current.file)) {
            isEditing = false
            refreshTickets()
        }
    }

    val selectedProjectEntry = projectsState.find { it.project.id == editableProjectId }
    val selectedProjectName = selectedProjectEntry?.project?.name
        ?: "No project"

    LaunchedEffect(selectedTicket?.file?.canonicalPath, selectedProjectEntry?.project?.id) {
        println("projec id to find: " + editableProjectId)
        projectsState.forEach {
            println("projec id: " + it.project.id)
        }
        val ticket = selectedTicket?.ticket ?: return@LaunchedEffect
        val projectStatus = if (selectedProjectEntry != null) "found" else "not found"
        println("Project $projectStatus for \"${ticket.title}\" (projectId ${ticket.projectId})")
    }


    val ticketsByStatus = TicketStatus.entries.mapNotNull { status ->
        val entries = ticketsState.filter { it.ticket.status == status }
        entries.takeIf { it.isNotEmpty() }?.let { status to it }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tickets", style = MaterialTheme.typography.titleLarge)
        Text(
            workingFolder?.let { "Working folder: $it" } ?: "Working folder not set",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.width(320.dp).fillMaxSize()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ticketsByStatus.isEmpty()) {
                        item {
                            Text(
                                "No tickets available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        ticketsByStatus.forEach { (status, entries) ->
                            item {
                                Text(
                                    status.displayText,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(entries) { entry ->
                                 val isSelected = entry.file.canonicalPath == selectedTicketPath
                                 Column(
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                         .clickable {
                                             if (isEditing) {
                                                 saveCurrentTicket()
                                             }
                                             isEditing = false
                                             selectedTicketPath = entry.file.canonicalPath
                                         }
                                         .padding(8.dp)
                                 ) {
                                     Text(entry.ticket.title, style = MaterialTheme.typography.titleMedium)
                                     SimpleDivider(modifier = Modifier.padding(top = 8.dp))
                                 }
                             }
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
                    Button(onClick = { createNewTicket() }, enabled = workingFolder != null) {
                        Text("New ticket")
                    }
                    if (selectedTicket != null) {
                        if (isEditing) {
                            Button(onClick = { saveCurrentTicket() }) { Text("Save") }
                        } else {
                            Button(onClick = { isEditing = true }) { Text("Edit") }
                        }
                        Button(onClick = { deleteCurrentTicket() }) { Text("Delete") }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (selectedTicket == null) {
                    Text(
                        "Select a ticket to view or edit it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    if (isEditing) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TextField(
                                value = editableTitle,
                                onValueChange = { editableTitle = it },
                                label = { Text("Ticket title") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = projectDropdownExpanded,
                                onExpandedChange = { projectDropdownExpanded = !projectDropdownExpanded }
                            ) {
                                TextField(
                                    value = selectedProjectName,
                                    onValueChange = {},
                                    label = { Text("Project") },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded) }
                                )
                                ExposedDropdownMenu(
                                    expanded = projectDropdownExpanded,
                                    onDismissRequest = { projectDropdownExpanded = false }
                                ) {
                                    projectsState.forEach { projectEntry ->
                                        DropdownMenuItem(
                                            text = { Text(projectEntry.project.name) },
                                            onClick = {
                                                editableProjectId = projectEntry.project.id
                                                projectDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = statusDropdownExpanded,
                                onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                            ) {
                                TextField(
                                    value = editableStatus,
                                    onValueChange = { editableStatus = it },
                                    label = { Text("Status") },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) }
                                )
                                ExposedDropdownMenu(
                                    expanded = statusDropdownExpanded,
                                    onDismissRequest = { statusDropdownExpanded = false }
                                ) {
                                    TicketStatus.entries.forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status.displayText) },
                                            onClick = {
                                                editableStatus = status.displayText
                                                statusDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
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
                            Text(selectedTicket.ticket.title, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Status: ${selectedTicket.ticket.status.displayText}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Project: $selectedProjectName",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            SelectionContainer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Markdown(
                                    selectedTicket.ticket.details.ifBlank { "*No details yet.*" }
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
fun TicketsPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent()
        }
    }
}
