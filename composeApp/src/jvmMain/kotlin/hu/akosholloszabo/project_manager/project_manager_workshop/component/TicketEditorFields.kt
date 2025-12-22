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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketEditorFields(
    projects: List<Pair<Int, String>>,
    title: StateAndEvent<String>,
    projectId: StateAndEvent<Int>,
    status: StateAndEvent<TicketStatus>,
    details: StateAndEvent<String>
) {
    var projectDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var statusDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    ) {
        TextField(
            value = title.state,
            onValueChange = title.event,
            label = { Text("Ticket title") },
            modifier = Modifier.fillMaxWidth()
        )
        ReadOnlyDropdownField(
            value = projects.toMap()[projectId.state] ?: "No project",
            label = "Project",
            expanded = projectDropdownExpanded,
            onExpandedChange = { projectDropdownExpanded = it }
        ) {
            projects.forEach { projectEntry ->
                DropdownMenuItem(
                    text = { Text(projectEntry.second) },
                    onClick = {
                        projectId.event(projectEntry.first)
                        projectDropdownExpanded = false
                    }
                )
            }
        }
        ReadOnlyDropdownField(
            value = status.state.displayText,
            label = "Status",
            expanded = statusDropdownExpanded,
            onExpandedChange = { statusDropdownExpanded = it }
        ) {
            TicketStatus.entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(entry.displayText) },
                    onClick = {
                        status.event(entry)
                        statusDropdownExpanded = false
                    }
                )
            }
        }
        TextField(
            value = details.state,
            onValueChange = details.event,
            label = { Text("Details (Markdown)") },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            maxLines = Int.MAX_VALUE
        )
    }
}
