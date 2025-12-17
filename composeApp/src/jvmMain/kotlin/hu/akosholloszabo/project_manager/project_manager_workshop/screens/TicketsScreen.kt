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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

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
    val columnBounds = remember { mutableStateMapOf<TicketStatus, Rect>() }

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
            }
            Button(onClick = { createNewTicket() }, enabled = workingFolder != null) {
                Text("New ticket")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            TicketBoard(
                ticketsByStatus = ticketsByStatus,
                projectNamesById = projectNamesById,
                selectedTicketPath = selectedTicketPath,
                onTicketSelected = { entry ->
                    if (isEditing) {
                        saveCurrentTicket()
                    }
                    isEditing = false
                    selectedTicketPath = entry.file.canonicalPath
                },
                onColumnPositioned = { status, rect -> columnBounds[status] = rect },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            if (selectedTicket != null) {
                Spacer(modifier = Modifier.width(16.dp))
                TicketDetailsPanel(
                    selectedTicket = selectedTicket,
                    selectedProjectName = selectedProjectName,
                    projects = projectsState,
                    isEditing = isEditing,
                    editableTitle = editableTitle,
                    onTitleChange = { editableTitle = it },
                    editableStatus = editableStatus,
                    onStatusChange = { editableStatus = it },
                    editableDetails = editableDetails,
                    onDetailsChange = { editableDetails = it },
                    projectDropdownExpanded = projectDropdownExpanded,
                    onProjectDropdownToggle = { projectDropdownExpanded = it },
                    statusDropdownExpanded = statusDropdownExpanded,
                    onStatusDropdownToggle = { statusDropdownExpanded = it },
                    onProjectSelected = { projectId ->
                        editableProjectId = projectId
                        projectDropdownExpanded = false
                    },
                    onStatusSelected = { status ->
                        editableStatus = status.displayText
                        statusDropdownExpanded = false
                    },
                    onSave = { saveCurrentTicket() },
                    onEditToggle = { isEditing = it },
                    onDelete = { deleteCurrentTicket() },
                    onBack = {
                        selectedTicketPath = null
                        isEditing = false
                    },
                    modifier = Modifier
                        .width(1024.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun TicketBoard(
    ticketsByStatus: Map<TicketStatus, List<TicketsStorage.PersistedTicket>>,
    projectNamesById: Map<Int, String>,
    selectedTicketPath: String?,
    onTicketSelected: (TicketsStorage.PersistedTicket) -> Unit,
    onColumnPositioned: (TicketStatus, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardScrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(boardScrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TicketStatus.entries.forEach { status ->
            key(status) {
                TicketColumn(
                    status = status,
                    tickets = ticketsByStatus[status].orEmpty(),
                    projectNamesById = projectNamesById,
                    selectedTicketPath = selectedTicketPath,
                    onTicketSelected = onTicketSelected,
                    onColumnPositioned = { rect -> onColumnPositioned(status, rect) }
                )
            }
        }
    }
}

@Composable
private fun TicketColumn(
    status: TicketStatus,
    tickets: List<TicketsStorage.PersistedTicket>,
    projectNamesById: Map<Int, String>,
    selectedTicketPath: String?,
    onTicketSelected: (TicketsStorage.PersistedTicket) -> Unit,
    onColumnPositioned: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                onColumnPositioned(Rect(coords.positionInRoot(), coords.size.toSize()))
            }
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(status.displayText, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tickets.isEmpty()) {
                Text(
                    "No tickets yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                tickets.forEach { entry ->
                    val projectName = projectNamesById[entry.ticket.projectId] ?: "No project"
                    val isSelected = entry.file.canonicalPath == selectedTicketPath
                    TicketCard(
                        entry = entry,
                        projectName = projectName,
                        isSelected = isSelected,
                        onClick = { onTicketSelected(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    entry: TicketsStorage.PersistedTicket,
    projectName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketDetailsPanel(
    selectedTicket: TicketsStorage.PersistedTicket,
    selectedProjectName: String,
    projects: List<ProjectsStorage.PersistedProject>,
    isEditing: Boolean,
    editableTitle: String,
    onTitleChange: (String) -> Unit,
    editableStatus: String,
    onStatusChange: (String) -> Unit,
    editableDetails: String,
    onDetailsChange: (String) -> Unit,
    projectDropdownExpanded: Boolean,
    onProjectDropdownToggle: (Boolean) -> Unit,
    statusDropdownExpanded: Boolean,
    onStatusDropdownToggle: (Boolean) -> Unit,
    onProjectSelected: (Int) -> Unit,
    onStatusSelected: (TicketStatus) -> Unit,
    onSave: () -> Unit,
    onEditToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Ticket details", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onBack) {
                Text("Back")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isEditing) {
                Button(onClick = onSave) { Text("Save") }
            } else {
                Button(onClick = { onEditToggle(true) }) { Text("Edit") }
            }
            Button(onClick = onDelete) { Text("Delete") }
        }

        if (isEditing) {
            TicketEditorFields(
                editableTitle = editableTitle,
                onTitleChange = onTitleChange,
                selectedProjectName = selectedProjectName,
                projects = projects,
                projectDropdownExpanded = projectDropdownExpanded,
                onProjectDropdownToggle = onProjectDropdownToggle,
                onProjectSelected = onProjectSelected,
                editableStatus = editableStatus,
                onStatusChange = onStatusChange,
                statusDropdownExpanded = statusDropdownExpanded,
                onStatusDropdownToggle = onStatusDropdownToggle,
                onStatusSelected = onStatusSelected,
                editableDetails = editableDetails,
                onDetailsChange = onDetailsChange
            )
        } else {
            TicketDetailsView(
                selectedTicket = selectedTicket,
                selectedProjectName = selectedProjectName
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketEditorFields(
    editableTitle: String,
    onTitleChange: (String) -> Unit,
    selectedProjectName: String,
    projects: List<ProjectsStorage.PersistedProject>,
    projectDropdownExpanded: Boolean,
    onProjectDropdownToggle: (Boolean) -> Unit,
    onProjectSelected: (Int) -> Unit,
    editableStatus: String,
    onStatusChange: (String) -> Unit,
    statusDropdownExpanded: Boolean,
    onStatusDropdownToggle: (Boolean) -> Unit,
    onStatusSelected: (TicketStatus) -> Unit,
    editableDetails: String,
    onDetailsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextField(
            value = editableTitle,
            onValueChange = onTitleChange,
            label = { Text("Ticket title") },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenuBox(
            expanded = projectDropdownExpanded,
            onExpandedChange = { onProjectDropdownToggle(!projectDropdownExpanded) }
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
                onDismissRequest = { onProjectDropdownToggle(false) }
            ) {
                projects.forEach { projectEntry ->
                    DropdownMenuItem(
                        text = { Text(projectEntry.project.name) },
                        onClick = { onProjectSelected(projectEntry.project.id) }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            expanded = statusDropdownExpanded,
            onExpandedChange = { onStatusDropdownToggle(!statusDropdownExpanded) }
        ) {
            TextField(
                value = editableStatus,
                onValueChange = onStatusChange,
                label = { Text("Status") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) }
            )
            ExposedDropdownMenu(
                expanded = statusDropdownExpanded,
                onDismissRequest = { onStatusDropdownToggle(false) }
            ) {
                TicketStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.displayText) },
                        onClick = { onStatusSelected(status) }
                    )
                }
            }
        }
        TextField(
            value = editableDetails,
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
private fun TicketDetailsView(
    selectedTicket: TicketsStorage.PersistedTicket,
    selectedProjectName: String
) {
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
            Markdown(selectedTicket.ticket.details.ifBlank { "*No details yet.*" })
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
