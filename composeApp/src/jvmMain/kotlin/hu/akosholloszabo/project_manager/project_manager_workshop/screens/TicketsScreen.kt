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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketBoard
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TicketDetailsPanel
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketsScreenContent(workingFolder: String) {
    val viewModel = remember(workingFolder) { TicketsViewModel(TicketStore(workingFolder), workingFolder) }
    LaunchedEffect(workingFolder) {
        viewModel.refresh()
    }
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Tickets", style = MaterialTheme.typography.titleLarge)
            }
            Button(onClick = viewModel::createTicket) {
                Text("New ticket")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            master = {
                key(uiState.boardVersion) {
                    TicketBoard(
                        columns = uiState.columns,
                        onTicketSelected = viewModel::selectTicket,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            },
            detail = if (uiState.selectedTicket != null || uiState.isEditing) {
                {
                    TicketDetailsPanel(
                        state = uiState,
                        onEdit = viewModel::startEditing,
                        onSave = viewModel::saveTicket,
                        onDelete = viewModel::deleteTicket,
                        onBack = viewModel::clearSelection,
                        onTicketChange = viewModel::updateEditorTicket,
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
