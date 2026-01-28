package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
fun TicketCard(
    modifier: Modifier = Modifier,
    cardState: TicketCardState,
    onClick: () -> Unit
) {
    val ticket = cardState.persisted.value
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (cardState.isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = ticket.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cardState.projectName,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TicketCardPreview() =
    PreviewWrapper(darkTheme = true) {
        TicketCard(
            cardState = TicketCardState(
                Persisted(File("preview"), Ticket(1, "Title", 1, TicketStatus.BACKLOG, "")),
                "Project",
                isSelected = true
            ),
            onClick = {}
        )
    }
