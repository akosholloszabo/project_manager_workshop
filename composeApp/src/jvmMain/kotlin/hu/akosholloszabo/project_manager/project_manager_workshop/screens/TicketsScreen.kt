package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
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
        val previousSelection = selectedTicketPath
        ticketsState.apply {
            clear()
            addAll(loaded)
        }
        selectedTicketPath = when {
            preservePath != null && loaded.any { it.file.canonicalPath == preservePath } -> preservePath
            previousSelection != null && loaded.any { it.file.canonicalPath == previousSelection } -> previousSelection
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
    val selectedProjectName = selectedProjectEntry?.project?.name ?: "No project"
    val projectNamesById = projectsState.associate { it.project.id to it.project.name }

    val ticketsByStatus = TicketStatus.entries.associateWith { status ->
        ticketsState.filter { it.ticket.status == status }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Tickets", style = MaterialTheme.typography.titleLarge)
                Text(
                    workingFolder?.let { "Working folder: $it" } ?: "Working folder not set",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(onClick = { createNewTicket() }, enabled = workingFolder != null) {
                Text("New ticket")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            val boardScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .horizontalScroll(boardScrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TicketStatus.entries.forEach { status ->
                    key(status) {
                        Column(
                            modifier = Modifier
                                .width(260.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(status.displayText, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            val entries = ticketsByStatus[status].orEmpty()
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (entries.isEmpty()) {
                                    Text(
                                        "No tickets yet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    entries.forEach { entry ->
                                        val projectName = projectNamesById[entry.ticket.projectId] ?: "No project"
                                        val isSelected = entry.file.canonicalPath == selectedTicketPath
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                else MaterialTheme.colorScheme.surface
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isEditing) {
                                                        saveCurrentTicket()
                                                    }
                                                    isEditing = false
                                                    selectedTicketPath = entry.file.canonicalPath
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    entry.ticket.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    projectName,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedTicket != null) {
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .width(1024.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ticket details", style = MaterialTheme.typography.titleMedium)
                        Button(onClick = {
                            selectedTicketPath = null
                            isEditing = false
                        }) {
                            Text("Back")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isEditing) {
                            Button(onClick = { saveCurrentTicket() }) { Text("Save") }
                        } else {
                            Button(onClick = { isEditing = true }) { Text("Edit") }
                        }
                        Button(onClick = { deleteCurrentTicket() }) { Text("Delete") }
                    }

                    if (isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = editableTitle,
                                onValueChange = { editableTitle = it },
                                label = { Text("Ticket title") },
                                modifier = Modifier.fillMaxWidth()
                            )
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
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(selectedTicket.ticket.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                "Status: ${selectedTicket.ticket.status.displayText}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Project: $selectedProjectName",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            SelectionContainer(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
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
