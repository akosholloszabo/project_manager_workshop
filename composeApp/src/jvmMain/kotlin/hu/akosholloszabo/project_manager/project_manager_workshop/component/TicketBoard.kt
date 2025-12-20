package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage

@Composable
fun TicketBoard(
    columns: List<TicketColumnState>,
    onTicketSelected: (TicketsStorage.PersistedTicket) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardScrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(boardScrollState),
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


