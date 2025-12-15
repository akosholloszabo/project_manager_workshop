package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TicketsScreenContent() {
    val tickets = remember {
        listOf(
            Ticket(1, "Landing page bug", 1, "Open"),
            Ticket(2, "Login fails on iOS", 2, "In Progress"),
            Ticket(3, "Add export feature", 3, "Done")
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tickets", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tickets.size) { ticket_index ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { /* placeholder */ }
                        .padding(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tickets[ticket_index].title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Project ID: ${tickets[ticket_index].projectId}")
                        }
                        Text(tickets[ticket_index].status)
                    }
                    hu.akosholloszabo.project_manager.project_manager_workshop.SimpleDivider(
                        modifier = Modifier.padding(
                            top = 8.dp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun TicketsPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            TicketsScreenContent()
        }
    }
}
