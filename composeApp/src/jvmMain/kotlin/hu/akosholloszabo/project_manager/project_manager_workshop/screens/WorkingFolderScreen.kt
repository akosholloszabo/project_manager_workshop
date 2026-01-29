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
import androidx.compose.material3.Scaffold
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
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.button_choose_folder
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.button_clear
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.button_continue
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.working_folder_label
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.working_folder_password_label
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.working_folder_password_placeholder
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.working_folder_placeholder
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.working_folder_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WorkingFolderScreen(
    workingFolderViewModel: WorkingFolderViewModel,
    onContinue: () -> Unit,
) {
    val selectedFolder by workingFolderViewModel.selectedFolder.collectAsStateWithLifecycle()
    var pendingFolder by rememberSaveable { mutableStateOf(selectedFolder) }
    var password by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val requiresPassword = workingFolderViewModel.requiresPassword
    val canContinue = pendingFolder != null && (password.isNotBlank() || !requiresPassword)

    LaunchedEffect(selectedFolder) {
        pendingFolder = selectedFolder
    }

    WorkingFolderScreenContent(
        pendingFolder = pendingFolder,
        onPendingFolderChange = { pendingFolder = it },
        password = password,
        onPasswordChange = { password = it },
        requiresPassword = requiresPassword,
        canContinue = canContinue,
        onChooseFolder = {
            scope.launch {
                FileChooser.chooseDirectory()?.let { pendingFolder = it }
            }
        },
        onConfirm = {
            pendingFolder?.let {
                workingFolderViewModel.confirmFolder(it, password)
                password = ""
                onContinue()
            }
        },
        onClear = {
            pendingFolder = null
            password = ""
            workingFolderViewModel.clearSelection()
        }
    )
}

@Composable
fun WorkingFolderScreenContent(
    pendingFolder: String?,
    onPendingFolderChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    requiresPassword: Boolean,
    canContinue: Boolean,
    onChooseFolder: () -> Unit,
    onConfirm: () -> Unit,
    onClear: () -> Unit,
) {

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.working_folder_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pendingFolder.orEmpty(),
                onValueChange = onPendingFolderChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.working_folder_label)) },
                placeholder = { Text(stringResource(Res.string.working_folder_placeholder)) },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            if (requiresPassword) {
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.working_folder_password_label)) },
                    placeholder = { Text(stringResource(Res.string.working_folder_password_placeholder)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
                Spacer(Modifier.height(16.dp))
            }

            Button(onClick = onChooseFolder) {
                Text(stringResource(Res.string.button_choose_folder))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onConfirm,
                    enabled = canContinue
                ) {
                    Text(stringResource(Res.string.button_continue))
                }
                TextButton(onClick = onClear) {
                    Text(stringResource(Res.string.button_clear))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun WorkingFolderScreenPreviewLight() {
    PreviewWrapper(darkTheme = false) {
        var pendingFolder by rememberSaveable { mutableStateOf("C:/Projects") }
        var password by rememberSaveable { mutableStateOf("") }
        WorkingFolderScreenContent(
            pendingFolder = pendingFolder,
            onPendingFolderChange = { pendingFolder = it },
            password = password,
            onPasswordChange = { password = it },
            requiresPassword = true,
            canContinue = password.isNotBlank(),
            onChooseFolder = {},
            onConfirm = {},
            onClear = {
                pendingFolder = ""
                password = ""
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun WorkingFolderScreenPreviewDark() {
    PreviewWrapper(darkTheme = true) {
        var pendingFolder by rememberSaveable { mutableStateOf("C:/Projects") }
        var password by rememberSaveable { mutableStateOf("") }
        WorkingFolderScreenContent(
            pendingFolder = pendingFolder,
            onPendingFolderChange = { pendingFolder = it },
            password = password,
            onPasswordChange = { password = it },
            requiresPassword = false,
            canContinue = true,
            onChooseFolder = {},
            onConfirm = {},
            onClear = {
                pendingFolder = ""
                password = ""
            }
        )
    }
}
