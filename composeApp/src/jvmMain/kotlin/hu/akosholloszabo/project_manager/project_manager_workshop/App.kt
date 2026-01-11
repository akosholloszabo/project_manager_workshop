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
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.text
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    storageBackend: StorageBackend,
    workingFolderViewModel: WorkingFolderViewModel?,
    notesViewModel: hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel,
    projectsViewModel: hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel,
    ticketsViewModel: hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel,
) {
    var currentScreen by rememberSaveable { mutableStateOf<Screen>(Screen.Notes) }
    val needsWorkingFolder = storageBackend != StorageBackend.SERVER
    val workingFolder by if (needsWorkingFolder) {
        workingFolderViewModel!!.selectedFolder.collectAsStateWithLifecycle()
    } else {
        remember { mutableStateOf(null) }
    }

    AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text("app.title"), modifier = Modifier.padding(0.dp))
                            if (needsWorkingFolder) {
                                workingFolder?.let {
                                    Text(
                                        text("working.folder.summary").format(it),
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
                        WorkingFolderScreen(
                            workingFolderViewModel = workingFolderViewModel!!,
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
                                    Text(text("app.tab.notes"))
                                }
                                Button(onClick = { currentScreen = Screen.Projects }) {
                                    Text(text("app.tab.projects"))
                                }
                                Button(onClick = { currentScreen = Screen.Tickets }) {
                                    Text(text("app.tab.tickets"))
                                }
                            }

                            SimpleDivider()

                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                when (currentScreen) {
                                    is Screen.Notes -> NotesScreen(notesViewModel = notesViewModel)
                                    is Screen.Projects -> ProjectsScreen(projectsViewModel = projectsViewModel)
                                    is Screen.Tickets -> TicketsScreen(ticketsViewModel = ticketsViewModel)
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
    App(
        storageBackend = StorageBackend.LOCAL,
        workingFolderViewModel = null,
        notesViewModel = hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel(
            hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore(
                null,
                object : hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage {
                    override fun loadNotes(session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?) =
                        emptyList<hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted<hu.akosholloszabo.project_manager.project_manager_workshop.model.Note>>()

                    override fun createNote(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        title: String?,
                        content: String
                    ) = null

                    override fun saveNoteContent(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        file: java.io.File,
                        content: String
                    ) = false

                    override fun deleteNote(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        file: java.io.File
                    ) = false
                })
        ),
        projectsViewModel = hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel(
            hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore(
                null,
                object : hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage {
                    override fun loadProjects(session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?) =
                        emptyList<hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted<hu.akosholloszabo.project_manager.project_manager_workshop.model.Project>>()

                    override fun createProject(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        name: String,
                        description: String,
                        details: String
                    ) = throw UnsupportedOperationException()

                    override fun saveProject(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        project: hu.akosholloszabo.project_manager.project_manager_workshop.model.Project,
                        file: java.io.File,
                        details: String
                    ) = false

                    override fun deleteProject(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        file: java.io.File
                    ) = false
                })
        ),
        ticketsViewModel = hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel(
            hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore(
                null,
                object : hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage {
                    override fun loadTickets(session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?) =
                        emptyList<hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted<hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket>>()

                    override fun createTicket(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        title: String,
                        projectId: Int,
                        status: hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus,
                        details: String
                    ) = null

                    override fun saveTicket(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        ticket: hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket,
                        file: java.io.File,
                        details: String
                    ) = false

                    override fun deleteTicket(
                        session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                        file: java.io.File
                    ) = false
                },
                hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend.LOCAL
            ),
            object : hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage {
                override fun loadProjects(session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?) =
                    emptyList<hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted<hu.akosholloszabo.project_manager.project_manager_workshop.model.Project>>()

                override fun createProject(
                    session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                    name: String,
                    description: String,
                    details: String
                ) = throw UnsupportedOperationException()

                override fun saveProject(
                    session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                    project: hu.akosholloszabo.project_manager.project_manager_workshop.model.Project,
                    file: java.io.File,
                    details: String
                ) = false

                override fun deleteProject(
                    session: hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession?,
                    file: java.io.File
                ) = false
            },
            null
        )
    )
}
