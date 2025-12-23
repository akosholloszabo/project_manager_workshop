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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.akosholloszabo.project_manager.project_manager_workshop.FileChooser
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkingFolderScreen(
    workingFolderViewModel: WorkingFolderViewModel,
    onContinue: () -> Unit
) {
    val selectedFolder by workingFolderViewModel.selectedFolder.collectAsStateWithLifecycle()
    var pendingFolder by rememberSaveable { mutableStateOf(selectedFolder) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedFolder) {
        pendingFolder = selectedFolder
    }

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
            value = pendingFolder ?: "",
            onValueChange = { pendingFolder = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Working folder") },
            placeholder = { Text("No folder selected yet") },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                FileChooser.chooseDirectory()?.let { pendingFolder = it }
            }
        }) {
            Text("Choose folder...")
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                pendingFolder?.let {
                    workingFolderViewModel.confirmFolder(it)
                    onContinue()
                }
            }, enabled = pendingFolder != null) {
                Text("Continue")
            }
            TextButton(onClick = {
                pendingFolder = null
                workingFolderViewModel.clearSelection()
            }) {
                Text("Clear")
            }
        }
    }
}
