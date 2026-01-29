package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_project_header
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_header
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_details_empty
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
fun TicketDetailsView(
    selectedTicket: Ticket,
    projectName: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = selectedTicket.title,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(Res.string.ticket_project_header, projectName),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = (selectedTicket.status?.resId?.let { stringResource(it) }
                ?: stringResource(Res.string.tickets_details_empty))
                .let { stringResource(Res.string.ticket_status_header, it) },
            style = MaterialTheme.typography.bodyMedium
        )
        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Markdown(selectedTicket.details.ifBlank {
                stringResource(Res.string.tickets_details_empty)
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TicketDetailsViewPreview() {
    val ticket = Persisted(File("preview"), Ticket(1, "Title", 1, TicketStatus.BACKLOG, "Details"))
    PreviewWrapper(darkTheme = true) {
        TicketDetailsView(
            selectedTicket = ticket.value,
            projectName = "Project Alpha"
        )
    }
}
