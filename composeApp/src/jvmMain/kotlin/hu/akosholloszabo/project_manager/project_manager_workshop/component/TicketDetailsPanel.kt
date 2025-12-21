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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketsScreenState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsPanel(
    state: TicketsScreenState,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    onTicketChange: (Ticket) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTicket = state.selectedTicket ?: return
    val editorTicket = state.editorTicket ?: selectedTicket.value
    val ticketKey = "${selectedTicket.file.canonicalPath}-${state.boardVersion}"
    var projectDropdownExpanded by rememberSaveable(ticketKey) { mutableStateOf(false) }
    var statusDropdownExpanded by rememberSaveable(ticketKey) { mutableStateOf(false) }
    val projectNamesById = state.projects.toMap()
    val displayProjectName = projectNamesById[selectedTicket.value.projectId] ?: "No project"

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
                            isEditing = state.isEditing,
                            onEdit = if (!state.isEditing) onEdit else null,
                            onSave = if (state.isEditing) onSave else null,
                            onDelete = onDelete
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onBack) {
                            Text("Back")
                        }
                    }
                }
            )
        },
        isEditing = state.isEditing,
        editContent = {
            TicketEditorFields(
                ticket = editorTicket,
                projects = state.projects,
                projectDropdownExpanded = projectDropdownExpanded,
                onProjectDropdownToggle = { projectDropdownExpanded = it },
                statusDropdownExpanded = statusDropdownExpanded,
                onStatusDropdownToggle = { statusDropdownExpanded = it },
                onTicketChange = onTicketChange
            )
        },
        viewContent = {
            TicketDetailsView(
                selectedTicket = selectedTicket.value,
                projectName = displayProjectName
            )
        }
    )
}
