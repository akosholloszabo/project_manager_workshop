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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_back
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_details_title
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_editor_no_project
import org.jetbrains.compose.resources.stringResource

@Composable
fun TicketDetailsPanel(
    modifier: Modifier = Modifier,
    selectedTicket: Persisted<Ticket>,
    projects: Map<Int, String>,
    isEditing: StateAndEvent<Boolean>,
    title: StateAndEvent<String>,
    projectId: StateAndEvent<Int>,
    status: StateAndEvent<TicketStatus?>,
    details: StateAndEvent<String>,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val projectNamesById = projects.toMap()

    DetailEditorPane(
        // TODO If you get a modifier from outside, you should not override
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
        // TODO Integer goes to resources
        verticalSpacing = 12.dp,
        header = {
            DetailHeader(
                title = stringResource(Res.string.tickets_details_title),
                actions = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CrudActionBar(
                            modifier = Modifier.weight(1f, false),
                            hasSelection = true,
                            isEditing = isEditing.state,
                            onEdit = { onEdit(); isEditing.event(true) },
                            onSave = if (isEditing.state) onSave else null,
                            onDelete = onDelete,
                        )
                        // TODO Integer goes to resources
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onBack) {
                            Text(stringResource(Res.string.tickets_back))
                        }
                    }
                }
            )
        },
        isEditing = isEditing.state,
        editContent = {
            TicketEditorFields(
                projects = projects,
                title = title,
                projectId = projectId,
                status = status,
                details = details,
            )
        },
        viewContent = {
            TicketDetailsView(
                selectedTicket = selectedTicket.value,
                projectName = projectNamesById[selectedTicket.value.projectId]
                    ?: stringResource(Res.string.tickets_editor_no_project)
            )
        }
    )
}

// TODO Preview
