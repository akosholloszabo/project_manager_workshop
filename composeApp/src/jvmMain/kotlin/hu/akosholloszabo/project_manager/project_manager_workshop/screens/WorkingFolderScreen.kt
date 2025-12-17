package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WorkingFolderScreen(
    selectedFolder: String?,
    onPickFolder: () -> Unit,
    onConfirm: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Select a working folder",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = selectedFolder ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Working folder") },
            placeholder = { Text("No folder selected yet") },
            readOnly = true,
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onPickFolder) {
            Text("Choose folder...")
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { selectedFolder?.let(onConfirm) }, enabled = selectedFolder != null) {
                Text("Continue")
            }
            TextButton(onClick = onClearSelection) {
                Text("Clear")
            }
        }
    }
}

