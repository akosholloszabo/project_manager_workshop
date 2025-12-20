package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketEditorFields(
    ticket: Ticket,
    projects: List<Pair<Int, String>>,
    projectDropdownExpanded: Boolean,
    onProjectDropdownToggle: (Boolean) -> Unit,
    statusDropdownExpanded: Boolean,
    onStatusDropdownToggle: (Boolean) -> Unit,
    onTicketChange: (Ticket) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    ) {
        TextField(
            value = ticket.title,
            onValueChange = { onTicketChange(ticket.copy(title = it)) },
            label = { Text("Ticket title") },
            modifier = Modifier.fillMaxWidth()
        )
        ReadOnlyDropdownField(
            value = projects.toMap()[ticket.projectId] ?: "No project",
            label = "Project",
            expanded = projectDropdownExpanded,
            onExpandedChange = onProjectDropdownToggle
        ) {
            projects.forEach { projectEntry ->
                DropdownMenuItem(
                    text = { Text(projectEntry.second) },
                    onClick = {
                        onTicketChange(ticket.copy(projectId = projectEntry.first))
                        onProjectDropdownToggle(false)
                    }
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
                    onClick = {
                        onTicketChange(ticket.copy(status = status))
                        onStatusDropdownToggle(false)
                    }
                )
            }
        }
        TextField(
            value = ticket.details,
            onValueChange = { onTicketChange(ticket.copy(details = it)) },
            label = { Text("Details (Markdown)") },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            maxLines = Int.MAX_VALUE
        )
    }
}
