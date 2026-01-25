package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState

@Composable
fun TicketBoard(
    modifier: Modifier = Modifier,
    columns: List<TicketColumnState>,
    onTicketSelected: (Persisted<Ticket>) -> Unit
) {
    val boardScrollState = rememberScrollState()
    Row(
        // TODO If you get a modifier from outside, you should not override
        modifier = modifier.horizontalScroll(boardScrollState),
        // TODO Integer goes to resources
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        columns.forEach { column ->
            key(column.status) {
                TicketColumn(
                    columnState = column,
                    onTicketSelected = onTicketSelected
                )
            }
        }
    }
}

// TODO Preview
