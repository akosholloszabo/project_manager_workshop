package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.akosholloszabo.project_manager.project_manager_workshop.FileChooser
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkingFolderScreen(
    workingFolderViewModel: WorkingFolderViewModel,
    onContinue: () -> Unit,
) {
    val selectedFolder by workingFolderViewModel.selectedFolder.collectAsStateWithLifecycle()
    var pendingFolder by rememberSaveable { mutableStateOf(selectedFolder) }
    var password by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedFolder) {
        pendingFolder = selectedFolder
    }

    val canContinue = pendingFolder != null && (password.isNotBlank() || !workingFolderViewModel.requiresPassword)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            getText("working.folder.title"),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = pendingFolder ?: "",
            onValueChange = { pendingFolder = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(getText("working.folder.label")) },
            placeholder = { Text(getText("working.folder.placeholder")) },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        if (workingFolderViewModel.requiresPassword) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(getText("working.folder.password.label")) },
                placeholder = { Text(getText("working.folder.password.placeholder")) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(Modifier.height(16.dp))
        }

        Button(onClick = {
            scope.launch {
                FileChooser.chooseDirectory()?.let { pendingFolder = it }
            }
        }) {
            Text(getText("button.choose.folder"))
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                pendingFolder?.let {
                    workingFolderViewModel.confirmFolder(it, password)
                    password = ""
                    onContinue()
                }
            }, enabled = canContinue) {
                Text(getText("button.continue"))
            }
            TextButton(onClick = {
                pendingFolder = null
                password = ""
                workingFolderViewModel.clearSelection()
            }) {
                Text(getText("button.clear"))
            }
        }
    }
}
