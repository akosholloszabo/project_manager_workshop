package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState

@Composable
fun TicketCard(
    cardState: TicketCardState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ticket = cardState.persisted.ticket
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (cardState.isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                ticket.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                cardState.projectName,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
