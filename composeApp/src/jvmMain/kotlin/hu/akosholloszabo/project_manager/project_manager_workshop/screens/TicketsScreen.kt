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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.actions.CrudAction
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.ReadOnlyDropdownField
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreenContent(workingFolder: String) {
    val boardState = rememberTicketBoardState(workingFolder)
    val activeTicket = boardState.selectedTicket

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Tickets", style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = { boardState.handleAction(CrudAction.Create) }) {
                Text("New ticket")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            master = {
                key(boardState.ticketBoardVersion) {
                    TicketBoard(
                        columns = boardState.columns,
                        onTicketSelected = boardState::selectTicket,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            },
            detail = if (activeTicket != null) {
                {
                    TicketDetailsPanel(
                        selectedTicket = activeTicket,
                        projects = boardState.projects,
                        projectNamesById = boardState.projectNamesById,
                        startInEdit = boardState.shouldStartEditingSelected,
                        onPendingEditConsumed = boardState::consumePendingEdit,
                        onTicketSaved = boardState::onTicketSaved,
                        onAction = boardState::handleAction,
                        onSave = boardState::saveCurrentTicket,
                        onDelete = boardState::deleteCurrentTicket,
                        onBack = boardState::clearSelection,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else null
        )
    }
}

@Composable
private fun TicketBoard(
    columns: List<TicketColumnState>,
    onTicketSelected: (TicketsStorage.PersistedTicket) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardScrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(boardScrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        columns.forEach { column ->
            key(column.status) {
                TicketColumn(
                    columnState = column,
                    onTicketSelected = onTicketSelected
                )
            }
        }
    }
}

@Composable
private fun TicketColumn(
    columnState: TicketColumnState,
    onTicketSelected: (TicketsStorage.PersistedTicket) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(columnState.status.displayText, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (columnState.cards.isEmpty()) {
                Text(
                    "No tickets yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                columnState.cards.forEach { card ->
                    TicketCard(
                        cardState = card,
                        onClick = { onTicketSelected(card.persisted) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TicketCard(
    cardState: TicketCardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ticket = cardState.persisted.ticket
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (cardState.isSelected) {
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
                ticket.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                cardState.projectName,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketDetailsPanel(
    selectedTicket: TicketsStorage.PersistedTicket,
    projects: List<ProjectSummary>,
    projectNamesById: Map<Int, String>,
    startInEdit: Boolean,
    onPendingEditConsumed: () -> Unit,
    onTicketSaved: (Ticket) -> Unit,
    onAction: (CrudAction) -> Unit,
    onSave: (Ticket) -> Ticket?,
    onDelete: () -> Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ticketKey = selectedTicket.file.canonicalPath
    var isEditing by rememberSaveable(ticketKey) { mutableStateOf(false) }
    var editorTicket by rememberSaveable(ticketKey, stateSaver = TicketSaver) {
        mutableStateOf(selectedTicket.ticket)
    }
    var projectDropdownExpanded by rememberSaveable(ticketKey) { mutableStateOf(false) }
    var statusDropdownExpanded by rememberSaveable(ticketKey) { mutableStateOf(false) }

    LaunchedEffect(selectedTicket.ticket, isEditing) {
        if (!isEditing) {
            editorTicket = selectedTicket.ticket
        }
    }

    LaunchedEffect(ticketKey, startInEdit) {
        if (startInEdit) {
            isEditing = true
            editorTicket = selectedTicket.ticket
            onPendingEditConsumed()
        }
    }

    val displayProjectName = projectNamesById[selectedTicket.ticket.projectId] ?: "No project"
    val editorProjectName = projectNamesById[editorTicket.projectId] ?: "No project"

    DetailEditorPane(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalSpacing = 12.dp,
        header = {
            DetailHeader(
                title = "Ticket details",
                actions = {
                    Column {
                        Button(onClick = {
                            isEditing = false
                            onBack()
                        }) {
                            Text("Back")
                        }
                        CrudActionBar(
                            modifier = Modifier.fillMaxWidth(),
                            hasSelection = true,
                            isEditing = isEditing,
                            onEdit = {
                                isEditing = true
                                onAction(CrudAction.Edit)
                            },
                            onSave = {
                                val updated = onSave(editorTicket)
                                if (updated != null) {
                                    editorTicket = updated
                                    isEditing = false
                                    onTicketSaved(updated)
                                }
                            },
                            onDelete = {
                                if (onDelete()) {
                                    onPendingEditConsumed()
                                    onBack()
                                }
                            }
                        )
                    }
                }
            )
        },
        isEditing = isEditing,
        editContent = {
            TicketEditorFields(
                ticket = editorTicket,
                projects = projects,
                projectName = editorProjectName,
                projectDropdownExpanded = projectDropdownExpanded,
                onProjectDropdownToggle = { projectDropdownExpanded = it },
                onProjectSelected = { projectId ->
                    editorTicket = editorTicket.copy(projectId = projectId)
                    projectDropdownExpanded = false
                },
                statusDropdownExpanded = statusDropdownExpanded,
                onStatusDropdownToggle = { statusDropdownExpanded = it },
                onStatusSelected = { status ->
                    editorTicket = editorTicket.copy(status = status)
                    statusDropdownExpanded = false
                },
                onTitleChange = { editorTicket = editorTicket.copy(title = it) },
                onDetailsChange = { editorTicket = editorTicket.copy(details = it) }
            )
        },
        viewContent = {
            TicketDetailsView(
                selectedTicket = selectedTicket.ticket,
                projectName = displayProjectName
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketEditorFields(
    ticket: Ticket,
    projects: List<ProjectSummary>,
    projectName: String,
    projectDropdownExpanded: Boolean,
    onProjectDropdownToggle: (Boolean) -> Unit,
    onProjectSelected: (Int) -> Unit,
    statusDropdownExpanded: Boolean,
    onStatusDropdownToggle: (Boolean) -> Unit,
    onStatusSelected: (TicketStatus) -> Unit,
    onTitleChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    ) {
        TextField(
            value = ticket.title,
            onValueChange = onTitleChange,
            label = { Text("Ticket title") },
            modifier = Modifier.fillMaxWidth()
        )
        ReadOnlyDropdownField(
            value = projectName,
            label = "Project",
            expanded = projectDropdownExpanded,
            onExpandedChange = onProjectDropdownToggle
        ) {
            projects.forEach { projectEntry ->
                DropdownMenuItem(
                    text = { Text(projectEntry.name) },
                    onClick = { onProjectSelected(projectEntry.id) }
                )
            }
        }
        ReadOnlyDropdownField(
            value = ticket.status.displayText,
            label = "Status",
            expanded = statusDropdownExpanded,
            onExpandedChange = onStatusDropdownToggle
        ) {
            TicketStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.displayText) },
                    onClick = { onStatusSelected(status) }
                )
            }
        }
        TextField(
            value = ticket.details,
            onValueChange = onDetailsChange,
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
    selectedTicket: Ticket,
    projectName: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(selectedTicket.title, style = MaterialTheme.typography.titleLarge)
        Text(
            "Status: ${selectedTicket.status.displayText}",
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
            Markdown(selectedTicket.details.ifBlank { "*No details yet.*" })
        }
    }
}

private val TicketSaver: Saver<Ticket, Any> = Saver(
    save = { ticket ->
        listOf(ticket.id, ticket.title, ticket.projectId, ticket.status.name, ticket.details)
    },
    restore = { raw ->
        val data = raw as List<*>
        Ticket(
            id = data[0] as Int,
            title = data[1] as String,
            projectId = data[2] as Int,
            status = TicketStatus.valueOf(data[3] as String),
            details = data[4] as String
        )
    }
)

private data class ProjectSummary(val id: Int, val name: String)

private data class TicketColumnState(
    val status: TicketStatus,
    val cards: List<TicketCardState>
)

private data class TicketCardState(
    val persisted: TicketsStorage.PersistedTicket,
    val projectName: String,
    val isSelected: Boolean
)

@Composable
private fun rememberTicketBoardState(workingFolder: String): TicketBoardState {
    val state = remember(workingFolder) { TicketBoardState(workingFolder) }
    LaunchedEffect(state, workingFolder) {
        state.loadInitialData()
    }
    return state
}

private class TicketBoardState(private val workingFolder: String) {
    private val projectsState = mutableStateListOf<ProjectSummary>()
    private val ticketsState = mutableStateListOf<TicketsStorage.PersistedTicket>()

    val projects: List<ProjectSummary>
        get() = projectsState

    val projectNamesById: Map<Int, String>
        get() = projectsState.associate { it.id to it.name }

    var selectedTicket by mutableStateOf<TicketsStorage.PersistedTicket?>(null)
        private set

    private var pendingEditTicket by mutableStateOf<TicketsStorage.PersistedTicket?>(null)

    var ticketBoardVersion by mutableStateOf(0)
        private set

    val shouldStartEditingSelected: Boolean
        get() = selectedTicket?.file?.canonicalPath == pendingEditTicket?.file?.canonicalPath

    val columns: List<TicketColumnState>
        get() {
            val projectNames = projectNamesById
            val selectedPath = selectedTicket?.file?.canonicalPath
            return TicketStatus.entries.map { status ->
                val cards = ticketsState
                    .filter { it.ticket.status == status }
                    .map { persisted ->
                        TicketCardState(
                            persisted = persisted,
                            projectName = projectNames[persisted.ticket.projectId] ?: "No project",
                            isSelected = persisted.file.canonicalPath == selectedPath
                        )
                    }
                TicketColumnState(status = status, cards = cards)
            }
        }

    fun loadInitialData() {
        refreshProjects()
        refreshTickets()
    }

    fun handleAction(action: CrudAction) {
        when (action) {
            CrudAction.Create -> createNewTicket()
            CrudAction.Edit -> pendingEditTicket = selectedTicket
            CrudAction.Save -> selectedTicket?.let { current ->
                saveCurrentTicket(current.ticket)
                pendingEditTicket = null
            }

            CrudAction.Delete -> if (deleteCurrentTicket()) {
                pendingEditTicket = null
                selectedTicket = null
            }
        }
    }

    fun selectTicket(entry: TicketsStorage.PersistedTicket) {
        selectedTicket = entry
    }

    fun clearSelection() {
        selectedTicket = null
        pendingEditTicket = null
    }

    fun consumePendingEdit() {
        pendingEditTicket = null
    }

    fun onTicketSaved(@Suppress("UNUSED_PARAMETER") ticket: Ticket) {
        pendingEditTicket = null
    }

    fun saveCurrentTicket(draft: Ticket): Ticket? {
        val current = selectedTicket ?: return null
        val trimmedTitle = draft.title.trim().ifEmpty { current.ticket.title }
        val updated = current.ticket.copy(
            title = trimmedTitle,
            projectId = draft.projectId,
            status = draft.status,
            details = draft.details
        )
        return if (TicketsStorage.saveTicket(updated, current.file, draft.details)) {
            refreshTickets(preserve = current)
            updated
        } else {
            null
        }
    }

    fun deleteCurrentTicket(): Boolean {
        val current = selectedTicket ?: return false
        return if (TicketsStorage.deleteTicket(current.file)) {
            refreshTickets()
            true
        } else {
            false
        }
    }

    private fun createNewTicket() {
        val created = TicketsStorage.createTicket(workingFolder) ?: return
        pendingEditTicket = created
        refreshTickets(preserve = created)
    }

    private fun refreshProjects() {
        val loadedProjects = ProjectsStorage
            .loadProjects(workingFolder)
            .map { ProjectSummary(id = it.project.id, name = it.project.name) }
        projectsState.apply {
            clear()
            addAll(loadedProjects)
        }
    }

    private fun refreshTickets(preserve: TicketsStorage.PersistedTicket? = null) {
        val loaded = TicketsStorage.loadTickets(workingFolder)
        ticketsState.apply {
            clear()
            addAll(loaded)
        }
        selectedTicket = resolveTicketMatch(preserve ?: selectedTicket, loaded)
        pendingEditTicket = resolveTicketMatch(pendingEditTicket, loaded)
        ticketBoardVersion++
    }

    private fun resolveTicketMatch(
        target: TicketsStorage.PersistedTicket?,
        candidates: List<TicketsStorage.PersistedTicket>
    ): TicketsStorage.PersistedTicket? {
        target ?: return null
        return candidates.firstOrNull { it.file.canonicalPath == target.file.canonicalPath }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewLight() {
    AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent(workingFolder = "preview")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent(workingFolder = "preview")
        }
    }
}
