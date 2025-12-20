package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.actions.CrudAction
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketBoard
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketDetailsPanel
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketBoardStateHolder
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreenContent(workingFolder: String) {
    val boardState = remember(workingFolder) { TicketBoardStateHolder(workingFolder) }
    LaunchedEffect(boardState, workingFolder) {
        boardState.loadInitialData()
    }
    val activeTicket = boardState.selectedTicket

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Tickets", style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = { boardState.handleAction(CrudAction.Create) }) {
                Text("New ticket")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            master = {
                key(boardState.ticketBoardVersion) {
                    TicketBoard(
                        columns = boardState.columns,
                        onTicketSelected = boardState::selectTicket,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            },
            detail = if (activeTicket != null) {
                {
                    TicketDetailsPanel(
                        boardState = boardState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else null
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewLight() {
    AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent(workingFolder = "preview")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent(workingFolder = "preview")
        }
    }
}
