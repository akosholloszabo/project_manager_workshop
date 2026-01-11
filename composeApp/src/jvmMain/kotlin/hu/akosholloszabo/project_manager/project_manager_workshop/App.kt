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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.NotesScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.ProjectsScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.TicketsScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.WorkingFolderScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getTextOrException
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import java.text.MessageFormat.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var currentScreen by rememberSaveable { mutableStateOf<Screen>(Screen.Notes) }
    val storageBackend: StorageBackend = koinInject()
    val needsWorkingFolder = storageBackend != StorageBackend.SERVER
    val workingFolderViewModel: WorkingFolderViewModel = koinInject()
    val workingFolder by workingFolderViewModel.selectedFolder.collectAsStateWithLifecycle()

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(getKoin().getTextOrException("app.title"), modifier = Modifier.padding(0.dp))
                            workingFolder?.let {
                                Text(
                                    getKoin().getTextOrException("working.folder.summary").format(it),
                                    modifier = Modifier.padding(0.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(),
                    modifier = Modifier.padding(0.dp)
                )
            },
            content = { innerPadding: PaddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (needsWorkingFolder && workingFolder == null) {
                        WorkingFolderScreen(
                            workingFolderViewModel = workingFolderViewModel,
                            onContinue = { currentScreen = Screen.Notes },
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { currentScreen = Screen.Notes }) {
                                    Text(getKoin().getTextOrException("app.tab.notes"))
                                }
                                Button(onClick = { currentScreen = Screen.Projects }) {
                                    Text(getKoin().getTextOrException("app.tab.projects"))
                                }
                                Button(onClick = { currentScreen = Screen.Tickets }) {
                                    Text(getKoin().getTextOrException("app.tab.tickets"))
                                }
                            }

                            SimpleDivider()

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                when (currentScreen) {
                                    is Screen.Notes -> {
                                        val notesViewModel: NotesViewModel = koinInject()
                                        NotesScreen(notesViewModel = notesViewModel)
                                    }

                                    is Screen.Projects -> {
                                        val projectsViewModel: ProjectsViewModel = koinInject()
                                        ProjectsScreen(projectsViewModel = projectsViewModel)
                                    }

                                    is Screen.Tickets -> {
                                        val ticketsViewModel: TicketsViewModel = koinInject()
                                        TicketsScreen(ticketsViewModel = ticketsViewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun AppPreview() {
    App()
}
