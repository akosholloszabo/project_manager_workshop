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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketBoard
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketDetailsPanel
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_new
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreen(ticketsViewModel: TicketsViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val columns by ticketsViewModel.columns.collectAsStateWithLifecycle()
    val selectedTicket by ticketsViewModel.selectedTicket.collectAsStateWithLifecycle()
    val projects by ticketsViewModel.projects.collectAsStateWithLifecycle()
    val isEditing by ticketsViewModel.isEditing.collectAsStateWithLifecycle()
    val title by ticketsViewModel.editTitle.collectAsStateWithLifecycle()
    val projectId by ticketsViewModel.editProjectId.collectAsStateWithLifecycle()
    val status by ticketsViewModel.editStatus.collectAsStateWithLifecycle()
    val details by ticketsViewModel.editDetails.collectAsStateWithLifecycle()

    LaunchedEffect(ticketsViewModel) {
        ticketsViewModel.refresh()
    }

    TicketsScreenContent(
        columns = columns,
        selectedTicket = selectedTicket,
        projects = projects,
        isEditing = StateAndEvent(value = isEditing) { shouldEdit -> if (shouldEdit) ticketsViewModel.startEditing() },
        title = StateAndEvent(value = title, event = ticketsViewModel::updateTitle),
        projectId = StateAndEvent(value = projectId, event = ticketsViewModel::updateProjectId),
        status = StateAndEvent(value = status, event = ticketsViewModel::updateStatus),
        details = StateAndEvent(value = details, event = ticketsViewModel::updateDetails),
        onTicketSelected = ticketsViewModel::selectTicket,
        onCreateTicket = { coroutineScope.launch { ticketsViewModel.createTicket() } },
        onEdit = ticketsViewModel::startEditing,
        onSave = { coroutineScope.launch { ticketsViewModel.saveTicket() } },
        onDelete = { coroutineScope.launch { ticketsViewModel.deleteTicket() } },
        onBack = ticketsViewModel::clearSelection
    )
}

@Composable
fun TicketsScreenContent(
    modifier: Modifier = Modifier,
    columns: List<TicketColumnState>,
    selectedTicket: Persisted<Ticket>?,
    projects: Map<Int, String>,
    isEditing: StateAndEvent<Boolean>,
    title: StateAndEvent<String>,
    projectId: StateAndEvent<Int>,
    status: StateAndEvent<TicketStatus?>,
    details: StateAndEvent<String>,
    onTicketSelected: (Persisted<Ticket>) -> Unit,
    onCreateTicket: () -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(modifier = modifier.padding(16.dp)) { padding ->
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(Res.string.tickets_title), style = MaterialTheme.typography.titleLarge)
                }
                Button(onClick = onCreateTicket) {
                    Text(stringResource(Res.string.tickets_new))
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
                detail = if (selectedTicket != null || isEditing.value) {
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
}

@Composable
private fun TicketsScreenPreviewContent(darkTheme: Boolean) {

    val previewTicket = Persisted(
        file = File("preview-ticket"),
        value = Ticket(
            id = 1,
            title = "Preview Ticket",
            projectId = 1,
            status = TicketStatus.BACKLOG,
            details = "Preview details"
        )
    )

    val previewColumns = TicketStatus.entries.map { status ->
        TicketColumnState(
            status = status,
            cards = if (status == TicketStatus.BACKLOG) {
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

    val previewProjects = mapOf(1 to "Preview project")
    val previewIsEditing = StateAndEvent(value = true)
    val previewTitleState = StateAndEvent(value = previewTicket.value.title)
    val previewProjectState = StateAndEvent(value = previewTicket.value.projectId)
    val previewStatusState = StateAndEvent(value = previewTicket.value.status)
    val previewDetailsState = StateAndEvent(value = previewTicket.value.details)

    PreviewWrapper(darkTheme = darkTheme) {
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

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewLight() = TicketsScreenPreviewContent(darkTheme = false)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewDark() = TicketsScreenPreviewContent(darkTheme = true)
