package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText

@Composable
fun TicketColumn(
    modifier: Modifier = Modifier,
    columnState: TicketColumnState,
    onTicketSelected: (Persisted<Ticket>) -> Unit
) {
    Column(
        // TODO If you get a modifier from outside, you should not override
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                // TODO Integer goes to resources
                RoundedCornerShape(12.dp)
            )
            // TODO Integer goes to resources
            .padding(12.dp)
    ) {
        // TODO If one parameter has named argument, all should have
        Text(columnState.status.displayName, style = MaterialTheme.typography.titleMedium)

        // TODO Integer goes to resources
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            // TODO Integer goes to resources
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (columnState.cards.isEmpty()) {
                Text(
                    getText("tickets.no.items"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                columnState.cards.forEach { card ->
                    TicketCard(
                        cardState = card,
                        onClick = { onTicketSelected(card.persisted) }
                    )
                }
            }
        }
    }
}

// TODO Preview
