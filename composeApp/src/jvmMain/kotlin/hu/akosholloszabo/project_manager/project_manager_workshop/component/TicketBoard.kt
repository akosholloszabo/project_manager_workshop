package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
fun TicketBoard(
    modifier: Modifier = Modifier,
    columns: List<TicketColumnState>,
    onTicketSelected: (Persisted<Ticket>) -> Unit
) {
    val boardScrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(boardScrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        columns.forEach { column ->
            key(column.status) {
                TicketColumn(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    columnState = column,
                    onTicketSelected = onTicketSelected,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TicketBoardPreview() =
    PreviewWrapper(darkTheme = true) {
        TicketBoard(
            columns = listOf(
                TicketColumnState(
                    status = TicketStatus.BACKLOG,
                    cards = listOf(
                        TicketCardState(
                            Persisted(File("preview"), Ticket(1, "Title", 1, TicketStatus.BACKLOG, "")),
                            projectName = "Project",
                            isSelected = false
                        )
                    )
                )
            ),
            onTicketSelected = {},
            modifier = Modifier.fillMaxHeight(),
        )
    }
