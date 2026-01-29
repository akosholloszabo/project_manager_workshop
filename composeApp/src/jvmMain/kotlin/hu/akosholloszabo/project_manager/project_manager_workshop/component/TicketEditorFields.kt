package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_details_empty
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_editor_no_project
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_field_details
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_field_project
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_field_status
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_field_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.StateAndEvent
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketEditorFields(
    projects: Map<Int, String>,
    title: StateAndEvent<String>,
    projectId: StateAndEvent<Int>,
    status: StateAndEvent<TicketStatus?>,
    details: StateAndEvent<String>
) {
    val spacing = 8.dp
    var projectDropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var statusDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = title.value,
            onValueChange = title.event,
            label = { Text(stringResource(Res.string.tickets_field_title)) },
            modifier = Modifier.fillMaxWidth()
        )
        ReadOnlyDropdownField(
            value = projects[projectId.value] ?: stringResource(Res.string.tickets_editor_no_project),
            label = stringResource(Res.string.tickets_field_project),
            expanded = projectDropdownExpanded,
            onExpandedChange = { projectDropdownExpanded = it },
            dropdownContent = {
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
        )
        ReadOnlyDropdownField(
            value = status.value?.resId?.let { stringResource(it) } ?: stringResource(Res.string.tickets_details_empty),
            label = stringResource(Res.string.tickets_field_status),
            expanded = statusDropdownExpanded,
            onExpandedChange = { statusDropdownExpanded = it },
            dropdownContent = {
                TicketStatus.entries.forEach { entry ->
                    DropdownMenuItem(
                        text = { Text(stringResource(entry.resId)) },
                        onClick = {
                            status.event(entry)
                            statusDropdownExpanded = false
                        }
                    )
                }
            }
        )
        TextField(
            value = details.value,
            onValueChange = details.event,
            modifier = Modifier.fillMaxSize(),
            label = { Text(stringResource(Res.string.tickets_field_details)) },
            maxLines = Int.MAX_VALUE
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TicketEditorFieldsPreview() {
    PreviewWrapper(darkTheme = true) {
        TicketEditorFields(
            projects = mapOf(1 to "Project Alpha"),
            title = StateAndEvent("Title") {},
            projectId = StateAndEvent(1) {},
            status = StateAndEvent(TicketStatus.BACKLOG) {},
            details = StateAndEvent("Details") {},
        )
    }
}
