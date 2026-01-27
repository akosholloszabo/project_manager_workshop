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
    modifier: Modifier = Modifier,
    cardState: TicketCardState,
    onClick: () -> Unit
) {
    val ticket = cardState.persisted.value
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (cardState.isSelected) {
                // TODO Integer goes to resources
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        // TODO If you get a modifier from outside, you should not override
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // TODO Integer goes to resources
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                // TODO If one parameter has named argument, all should have
                ticket.title,
                style = MaterialTheme.typography.titleMedium,
                // TODO Integer goes to resources
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // TODO Integer goes to resources
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                // TODO If one parameter has named argument, all should have
                cardState.projectName,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// TODO Preview
