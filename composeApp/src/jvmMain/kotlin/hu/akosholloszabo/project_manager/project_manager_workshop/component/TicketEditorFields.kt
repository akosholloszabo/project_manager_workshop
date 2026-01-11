package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.text


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketEditorFields(
    projects: Map<Int, String>,
    title: StateAndEvent<String>,
    projectId: StateAndEvent<Int>,
    status: StateAndEvent<TicketStatus?>,
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
            label = { Text(text("tickets.field.title")) },
            modifier = Modifier.fillMaxWidth()
        )
        ReadOnlyDropdownField(
            value = projects.toMap()[projectId.state] ?: text("tickets.editor.no.project"),
            label = text("tickets.field.project"),
            expanded = projectDropdownExpanded,
            onExpandedChange = { projectDropdownExpanded = it }
        ) {
            projects.forEach { projectEntry ->
                DropdownMenuItem(
                    text = { Text(projectEntry.value) },
                    onClick = {
                        projectId.event(projectEntry.key)
                        projectDropdownExpanded = false
                    }
                )
            }
        }
        ReadOnlyDropdownField(
            value = status.state?.displayName ?: text("tickets.status.missing"),
            label = text("tickets.field.status"),
            expanded = statusDropdownExpanded,
            onExpandedChange = { statusDropdownExpanded = it }
        ) {
            TicketStatus.entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(entry.displayName) },
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
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            label = { Text(text("tickets.field.details")) },
            maxLines = Int.MAX_VALUE
        )
    }
}
