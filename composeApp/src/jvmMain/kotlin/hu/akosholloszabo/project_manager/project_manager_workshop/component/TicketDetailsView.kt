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
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getTextOrException
import org.koin.compose.getKoin

@Composable
fun TicketDetailsView(
    selectedTicket: Ticket,
    projectName: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(selectedTicket.title, style = MaterialTheme.typography.titleLarge)
        Text(
            getKoin().getTextOrException("ticket.status.header")
                .format(selectedTicket.status?.displayName ?: getKoin().getTextOrException("tickets.status.missing")), style = MaterialTheme.typography.bodyMedium
        )
        Text(
            getKoin().getTextOrException("ticket.project.header")
                .format(projectName), style = MaterialTheme.typography.bodyMedium
        )
        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Markdown(selectedTicket.details.ifBlank {
                getKoin().getTextOrException("tickets.details.empty")
            })
        }
    }
}
