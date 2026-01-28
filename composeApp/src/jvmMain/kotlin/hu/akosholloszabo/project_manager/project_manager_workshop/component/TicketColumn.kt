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
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.tickets_no_items
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TicketColumn(
    modifier: Modifier = Modifier,
    columnState: TicketColumnState,
    onTicketSelected: (Persisted<Ticket>) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Text(stringResource(columnState.status.resId), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (columnState.cards.isEmpty()) {
                Text(
                    stringResource(Res.string.tickets_no_items),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                columnState.cards.forEach { card ->
                    TicketCard(
                        cardState = card,
                        onClick = { onTicketSelected(card.persisted) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TicketColumnPreview() =
    PreviewWrapper(darkTheme = true) {
        TicketColumn(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
            columnState = TicketColumnState(
                status = TicketStatus.BACKLOG,
                cards = listOf(
                    TicketCardState(
                        persisted = Persisted(file = java.io.File("preview"), value = Ticket(1, "Title", 1, null, "")),
                        projectName = "Proj",
                        isSelected = false
                    )
                )
            ),
            onTicketSelected = {},
        )
    }
