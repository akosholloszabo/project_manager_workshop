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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.app_tab_notes
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.app_tab_projects
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.app_tab_tickets
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.app_title
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.working_folder_summary
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.NotesScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.ProjectsScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.TicketsScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.screens.WorkingFolderScreen
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.FileStorageHelper
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.PlainWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    storageBackend: StorageBackend,
    notesViewModel: NotesViewModel,
    projectsViewModel: ProjectsViewModel,
    ticketsViewModel: TicketsViewModel,
    workingFolderViewModel: WorkingFolderViewModel?,
) {
    var currentScreen by rememberSaveable { mutableStateOf<Screen>(Screen.Notes) }
    val needsWorkingFolder = storageBackend != StorageBackend.SERVER
    val workingFolder by if (needsWorkingFolder) {
        workingFolderViewModel?.selectedFolder?.collectAsState(initial = null)
            ?: remember { mutableStateOf(null) }
    } else {
        remember { mutableStateOf(null) }
    }

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(Res.string.app_title), modifier = Modifier.padding(0.dp))
                            if (needsWorkingFolder) {
                                workingFolder?.let {
                                    Text(
                                        stringResource(Res.string.working_folder_summary, it),
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
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
                        val wfVm: WorkingFolderViewModel = requireNotNull(workingFolderViewModel)
                        WorkingFolderScreen(
                            workingFolderViewModel = wfVm,
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
                                    Text(stringResource(Res.string.app_tab_notes))
                                }
                                Button(onClick = { currentScreen = Screen.Projects }) {
                                    Text(stringResource(Res.string.app_tab_projects))
                                }
                                Button(onClick = { currentScreen = Screen.Tickets }) {
                                    Text(stringResource(Res.string.app_tab_tickets))
                                }
                            }


                            HorizontalDivider()

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                when (currentScreen) {
                                    is Screen.Notes -> NotesScreen(notesViewModel)
                                    is Screen.Projects -> ProjectsScreen(projectsViewModel)
                                    is Screen.Tickets -> TicketsScreen(ticketsViewModel)
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
    val backend = StorageBackend.fromPropertyValue(Properties().apply {
        setProperty(
            "storage.backend",
            StorageBackend.LOCAL.name
        )
    }.getProperty("storage.backend"))
    val fileStorageHelper = FileStorageHelper()
    val workingFolderStore: WorkingFolderStore = PlainWorkingFolderStore()
    val workingFolderViewModel = WorkingFolderViewModel(workingFolderStore)
    val notesStorage = PlainNotesStorage(fileStorageHelper)
    val projectsStorage = PlainProjectsStorage(fileStorageHelper)
    val ticketsStorage = PlainTicketsStorage(fileStorageHelper)
    val noteStore = NoteStore(workingFolderStore, notesStorage)
    val projectStore = ProjectStore(workingFolderStore, projectsStorage)
    val ticketStore = TicketStore(workingFolderStore, ticketsStorage, backend)
    val notesViewModel = NotesViewModel(noteStore)
    val projectsViewModel = ProjectsViewModel(projectStore)
    val ticketsViewModel = TicketsViewModel(ticketStore, projectsStorage, workingFolderStore)
    App(
        storageBackend = backend,
        notesViewModel = notesViewModel,
        projectsViewModel = projectsViewModel,
        ticketsViewModel = ticketsViewModel,
        workingFolderViewModel = workingFolderViewModel,
    )
}
