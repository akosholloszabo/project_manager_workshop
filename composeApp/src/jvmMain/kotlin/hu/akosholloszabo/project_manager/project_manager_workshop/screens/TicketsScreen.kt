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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketBoard
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketDetailsPanel
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(workingFolder: String) {
    val viewModel = remember(workingFolder) { TicketsViewModel(workingFolder) }
    LaunchedEffect(workingFolder) {
        viewModel.refresh()
    }
    val columns by viewModel.columns.collectAsStateWithLifecycle()
    val selectedTicket by viewModel.selectedTicket.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val isEditing by viewModel.isEditing.collectAsStateWithLifecycle()
    val title by viewModel.editTitle.collectAsStateWithLifecycle()
    val projectId by viewModel.editProjectId.collectAsStateWithLifecycle()
    val status by viewModel.editStatus.collectAsStateWithLifecycle()
    val details by viewModel.editDetails.collectAsStateWithLifecycle()

    val isEditingState = StateAndEvent(state = isEditing) { shouldEdit -> if (shouldEdit) viewModel.startEditing() }
    val titleState = StateAndEvent(state = title, event = {
        viewModel.editTitle.tryEmit(it)
    })
    val projectState = StateAndEvent(state = projectId, event = {
        viewModel.editProjectId.tryEmit(it)
    })
    val statusState = StateAndEvent(state = status, event = {
        viewModel.editStatus.tryEmit(it)
    })
    val detailsState = StateAndEvent(state = details, event = {
        viewModel.editDetails.tryEmit(it)
    })

    TicketsScreenContent(
        columns = columns,
        selectedTicket = selectedTicket,
        projects = projects,
        isEditing = isEditingState,
        title = titleState,
        projectId = projectState,
        status = statusState,
        details = detailsState,
        onTicketSelected = viewModel::selectTicket,
        onCreateTicket = viewModel::createTicket,
        onEdit = viewModel::startEditing,
        onSave = viewModel::saveTicket,
        onDelete = viewModel::deleteTicket,
        onBack = viewModel::clearSelection
    )
}

@Composable
fun TicketsScreenContent(
    columns: List<TicketColumnState>,
    selectedTicket: Persisted<Ticket>?,
    projects: List<Pair<Int, String>>,
    isEditing: StateAndEvent<Boolean>,
    title: StateAndEvent<String>,
    projectId: StateAndEvent<Int>,
    status: StateAndEvent<TicketStatus>,
    details: StateAndEvent<String>,
    onTicketSelected: (Persisted<Ticket>) -> Unit,
    onCreateTicket: () -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Tickets", style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = onCreateTicket) {
                Text("New ticket")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            master = {
                TicketBoard(
                    columns = columns,
                    onTicketSelected = onTicketSelected,
                    modifier = Modifier.fillMaxHeight()
                )
            },
            detail = if (selectedTicket != null || isEditing.state) {
                {
                    val selected = selectedTicket ?: return@TwoPaneLayout
                    TicketDetailsPanel(
                        selectedTicket = selected,
                        projects = projects,
                        isEditing = isEditing,
                        title = title,
                        projectId = projectId,
                        status = status,
                        details = details,
                        onEdit = onEdit,
                        onSave = onSave,
                        onDelete = onDelete,
                        onBack = onBack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else null
        )
    }
}

@Composable
private fun TicketsScreenPreviewContent(darkTheme: Boolean) {

    val previewTicket = Persisted(
        file = File("preview-ticket"),
        value = Ticket(
            id = 1,
            title = "Preview Ticket",
            projectId = 1,
            status = TicketStatus.Backlog,
            details = "Preview details"
        )
    )

    val previewColumns = TicketStatus.entries.map { status ->
        TicketColumnState(
            status = status,
            cards = if (status == TicketStatus.Backlog) {
                listOf(
                    TicketCardState(
                        persisted = previewTicket,
                        projectName = "Preview project",
                        isSelected = true
                    )
                )
            } else {
                emptyList()
            }
        )
    }

    val previewProjects = listOf(1 to "Preview project")
    val previewIsEditing = StateAndEvent(state = true)
    val previewTitleState = StateAndEvent(state = previewTicket.value.title)
    val previewProjectState = StateAndEvent(state = previewTicket.value.projectId)
    val previewStatusState = StateAndEvent(state = previewTicket.value.status)
    val previewDetailsState = StateAndEvent(state = previewTicket.value.details)

    AppTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent(
                columns = previewColumns,
                selectedTicket = previewTicket,
                projects = previewProjects,
                isEditing = previewIsEditing,
                title = previewTitleState,
                projectId = previewProjectState,
                status = previewStatusState,
                details = previewDetailsState,
                onTicketSelected = {},
                onCreateTicket = {},
                onEdit = {},
                onSave = {},
                onDelete = {},
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewLight() = TicketsScreenPreviewContent(darkTheme = false)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewDark() = TicketsScreenPreviewContent(darkTheme = true)
