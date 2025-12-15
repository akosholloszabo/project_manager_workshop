package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Notes) }

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Project Manager") },
                    colors = TopAppBarDefaults.topAppBarColors()
                )
            },
            content = { innerPadding: PaddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {

                    // Simple navigation row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = { currentScreen = Screen.Notes }) {
                            Text("Notes")
                        }
                        Button(onClick = { currentScreen = Screen.Projects }) {
                            Text("Projects")
                        }
                        Button(onClick = { currentScreen = Screen.Tickets }) {
                            Text("Tickets")
                        }
                    }

                    SimpleDivider()

                    // Content area
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        when (currentScreen) {
                            is Screen.Notes -> NotesScreenContent()
                            is Screen.Projects -> ProjectsScreenContent()
                            is Screen.Tickets -> TicketsScreenContent()
                        }
                    }
                }
            }
        )
    }
}
