package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.actions.CrudAction
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsPanel(
    selectedTicket: TicketsStorage.PersistedTicket,
    projects: List<Pair<Int, String>>,
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
        mutableStateOf(
            selectedTicket.ticket
        )
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

    val projectNamesById = projects.toMap()
    val displayProjectName = projectNamesById[selectedTicket.ticket.projectId] ?: "No project"

    DetailEditorPane(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalSpacing = 12.dp,
        header = {
            DetailHeader(
                title = "Ticket details",
                actions = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CrudActionBar(
                            modifier = Modifier.weight(1f, false),
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            isEditing = false
                            onBack()
                        }) {
                            Text("Back")
                        }
                    }
                }
            )
        },
        isEditing = isEditing,
        editContent = {
            TicketEditorFields(
                ticket = editorTicket,
                projects = projects,
                projectDropdownExpanded = projectDropdownExpanded,
                onProjectDropdownToggle = { projectDropdownExpanded = it },
                statusDropdownExpanded = statusDropdownExpanded,
                onStatusDropdownToggle = { statusDropdownExpanded = it },
                onTicketChange = { editorTicket = it }
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
