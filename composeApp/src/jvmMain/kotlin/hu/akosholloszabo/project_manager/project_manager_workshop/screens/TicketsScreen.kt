package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
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
    val selectedTicketProjectName = selectedTicket?.ticket?.projectId?.let { projectNamesById[it] } ?: "No project"

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
                val editorState = TicketEditorState(
                    title = editableTitle,
                    projectName = selectedProjectName,
                    projectDropdownExpanded = projectDropdownExpanded,
                    status = editableStatus,
                    statusDropdownExpanded = statusDropdownExpanded,
                    details = editableDetails
                )
                val editorCallbacks = TicketEditorCallbacks(
                    onTitleChange = { editableTitle = it },
                    onProjectDropdownToggle = { projectDropdownExpanded = it },
                    onProjectSelected = { projectId ->
                        editableProjectId = projectId
                        projectDropdownExpanded = false
                    },
                    onStatusDropdownToggle = { statusDropdownExpanded = it },
                    onStatusSelected = { status ->
                        editableStatus = status.displayText
                        statusDropdownExpanded = false
                    },
                    onDetailsChange = { editableDetails = it }
                )
                val detailsState = TicketDetailsState(
                    selectedTicket = selectedTicket,
                    projects = projectsState,
                    selectedProjectName = selectedProjectName,
                    displayProjectName = selectedTicketProjectName,
                    isEditing = isEditing,
                    editorState = editorState
                )
                val detailsCallbacks = TicketDetailsCallbacks(
                    editorCallbacks = editorCallbacks,
                    onSave = { saveCurrentTicket() },
                    onEditToggle = { isEditing = it },
                    onDelete = { deleteCurrentTicket() },
                    onBack = {
                        selectedTicketPath = null
                        isEditing = false
                    }
                )
                TicketDetailsPanel(
                    state = detailsState,
                    callbacks = detailsCallbacks,
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
    state: TicketDetailsState,
    callbacks: TicketDetailsCallbacks,
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
            Button(onClick = callbacks.onBack) {
                Text("Back")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.isEditing) {
                Button(onClick = callbacks.onSave) { Text("Save") }
            } else {
                Button(onClick = { callbacks.onEditToggle(true) }) { Text("Edit") }
            }
            Button(onClick = callbacks.onDelete) { Text("Delete") }
        }

        if (state.isEditing) {
            TicketEditorFields(
                state = state.editorState,
                projects = state.projects,
                callbacks = callbacks.editorCallbacks
            )
        } else {
            TicketDetailsView(
                selectedTicket = state.selectedTicket,
                projectName = state.displayProjectName
            )
        }
    }
}

private data class TicketEditorState(
    val title: String,
    val projectName: String,
    val projectDropdownExpanded: Boolean,
    val status: String,
    val statusDropdownExpanded: Boolean,
    val details: String
)

private data class TicketEditorCallbacks(
    val onTitleChange: (String) -> Unit,
    val onProjectDropdownToggle: (Boolean) -> Unit,
    val onProjectSelected: (Int) -> Unit,
    val onStatusDropdownToggle: (Boolean) -> Unit,
    val onStatusSelected: (TicketStatus) -> Unit,
    val onDetailsChange: (String) -> Unit
)

private data class TicketDetailsState(
    val selectedTicket: TicketsStorage.PersistedTicket,
    val projects: List<ProjectsStorage.PersistedProject>,
    val selectedProjectName: String,
    val displayProjectName: String,
    val isEditing: Boolean,
    val editorState: TicketEditorState
)

private data class TicketDetailsCallbacks(
    val editorCallbacks: TicketEditorCallbacks,
    val onSave: () -> Unit,
    val onEditToggle: (Boolean) -> Unit,
    val onDelete: () -> Unit,
    val onBack: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketEditorFields(
    state: TicketEditorState,
    projects: List<ProjectsStorage.PersistedProject>,
    callbacks: TicketEditorCallbacks
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    ) {
        TextField(
            value = state.title,
            onValueChange = callbacks.onTitleChange,
            label = { Text("Ticket title") },
            modifier = Modifier.fillMaxWidth()
        )
        ReadOnlyDropdownField(
            value = state.projectName,
            label = "Project",
            expanded = state.projectDropdownExpanded,
            onExpandedChange = callbacks.onProjectDropdownToggle
        ) {
            projects.forEach { projectEntry ->
                DropdownMenuItem(
                    text = { Text(projectEntry.project.name) },
                    onClick = { callbacks.onProjectSelected(projectEntry.project.id) }
                )
            }
        }
        ReadOnlyDropdownField(
            value = state.status,
            label = "Status",
            expanded = state.statusDropdownExpanded,
            onExpandedChange = callbacks.onStatusDropdownToggle
        ) {
            TicketStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.displayText) },
                    onClick = { callbacks.onStatusSelected(status) }
                )
            }
        }
        TextField(
            value = state.details,
            onValueChange = callbacks.onDetailsChange,
            label = { Text("Details (Markdown)") },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            maxLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun TicketDetailsView(
    selectedTicket: TicketsStorage.PersistedTicket,
    projectName: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(selectedTicket.ticket.title, style = MaterialTheme.typography.titleLarge)
        Text(
            "Status: ${selectedTicket.ticket.status.displayText}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "Project: $projectName",
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
fun TicketsScreenPreviewLight() {
    AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadOnlyDropdownField(
    value: String,
    label: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    dropdownContent: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) }
    ) {
        val fillMaxWidth = Modifier
            .fillMaxWidth()
        TextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            modifier = fillMaxWidth
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.exposedDropdownSize()
        ) {
            dropdownContent()
        }
    }
}
