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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.WorkingFolderScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager

private val osNameLower = System.getProperty("os.name").lowercase()
private val isWindowsOs = osNameLower.contains("win")
private val isLinuxOs = osNameLower.contains("nux") || osNameLower.contains("nix") || osNameLower.contains("aix")

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun App() {
    var currentScreen by rememberSaveable { mutableStateOf<Screen>(Screen.Notes) }
    var startupComplete by rememberSaveable { mutableStateOf(false) }
    var workingFolder by rememberSaveable { mutableStateOf<String?>("D:\\git\\akosholloszabo\\project_manadger_workshop_2025_01\\samples") }

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Project Manager") },
                    colors = TopAppBarDefaults.topAppBarColors()
                )
            },
            content = { innerPadding: PaddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (!startupComplete) {
                        WorkingFolderScreen(
                            selectedFolder = workingFolder,
                            onPickFolder = {
                                pickWorkingFolder(workingFolder)?.let { folder ->
                                    workingFolder = folder
                                }
                            },
                            onConfirm = { folder ->
                                workingFolder = folder
                                startupComplete = true
                            },
                            onClearSelection = {
                                workingFolder = null
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
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

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                when (currentScreen) {
                                    is Screen.Notes -> NotesScreenContent(workingFolder)
                                    is Screen.Projects -> ProjectsScreenContent(workingFolder)
                                    is Screen.Tickets -> TicketsScreenContent(workingFolder)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

private fun pickWorkingFolder(initialPath: String?): String? {
    return when {
        isWindowsOs -> pickWithPowerShell(initialPath) ?: pickWithJFileChooser(initialPath)
        isLinuxOs -> pickWithZenity(initialPath) ?: pickWithJFileChooser(initialPath)
        else -> pickWithJFileChooser(initialPath)
    }
}

private fun pickWithJFileChooser(initialPath: String?): String? {
    val previousLaf = UIManager.getLookAndFeel()
    if (previousLaf?.javaClass?.name != UIManager.getSystemLookAndFeelClassName()) {
        runCatching { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) }
    }

    val chooser = JFileChooser().apply {
        dialogTitle = "Select working folder"
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        currentDirectory = initialPath?.let(::File) ?: File(System.getProperty("user.home"))
    }

    val result = if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile?.absolutePath
    } else {
        null
    }

    previousLaf?.let {
        runCatching { UIManager.setLookAndFeel(it) }
    }

    return result
}

private fun pickWithZenity(initialPath: String?): String? {
    val args = mutableListOf(
        "zenity",
        "--file-selection",
        "--directory",
        "--title",
        "Select working folder"
    )

    initialPath?.let {
        val normalized = File(it).absolutePath.trimEnd(File.separatorChar) + File.separator
        args += listOf("--filename", normalized)
    }

    return try {
        val process = ProcessBuilder(args)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readLine() }
        val exitCode = process.waitFor()

        if (exitCode == 0 && !output.isNullOrBlank()) {
            output.trim()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun pickWithPowerShell(initialPath: String?): String? {
    val safePath = escapeForPowerShell(initialPath ?: System.getProperty("user.home"))
    val script = listOf(
        "Add-Type -AssemblyName System.Windows.Forms",
        "\$dialog = New-Object System.Windows.Forms.FolderBrowserDialog",
        "\$dialog.SelectedPath = '$safePath'",
        "\$dialog.ShowNewFolderButton = \$true",
        "if (\$dialog.ShowDialog() -eq 'OK') { Write-Output \$dialog.SelectedPath }"
    ).joinToString("; ")

    return try {
        val process = ProcessBuilder("powershell.exe", "-NoProfile", "-STA", "-Command", script)
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().use { it.readLine() }
        val exitCode = process.waitFor()

        if (exitCode == 0 && !output.isNullOrBlank()) {
            output.trim()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun escapeForPowerShell(value: String): String {
    return value.replace("'", "''").replace("\r", "").replace("\n", "")
}
