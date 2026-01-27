package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.di.localModule
import hu.akosholloszabo.project_manager.project_manager_workshop.di.mainModule
import hu.akosholloszabo.project_manager.project_manager_workshop.di.plainLocalModule
import hu.akosholloszabo.project_manager.project_manager_workshop.model.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplicationPreview
import org.koin.fileProperties
import java.io.File

@Composable
// TODO you can inject ViewModel directly here
fun NotesScreen(notesViewModel: NotesViewModel) {
    // TODO first the variables
    LaunchedEffect(notesViewModel) {
        notesViewModel.refresh()
    }
    // TODO we usually use val viewmodel = koinViewModel<NotesViewModel>()
    val selectedNotePath by notesViewModel.selectedNotePath.collectAsStateWithLifecycle()
    val isEditing by notesViewModel.isEditing.collectAsStateWithLifecycle()
    val editableContent by notesViewModel.editableContent.collectAsStateWithLifecycle()
    val selectedNote by notesViewModel.selectedNote.collectAsStateWithLifecycle()
    val notes by notesViewModel.notes.collectAsStateWithLifecycle()

    NotesScreenContent(
        selectedNotePath = selectedNotePath,
        isEditing = StateAndEvent(
            state = isEditing,
            // TODO Screen should not know about the flow, only call functions on the ViewModel
            event = { notesViewModel.isEditing.tryEmit(it) }
        ),
        editableContent = StateAndEvent(
            state = editableContent,
            // TODO Screen should not know about the flow, only call functions on the ViewModel
            event = { notesViewModel.editableContent.tryEmit(it) }
        ),
        selectedNote = selectedNote,
        notes = notes,
        onCreateNote = notesViewModel::createNote,
        onSaveNote = notesViewModel::saveNote,
        onDeleteNote = notesViewModel::deleteNote,
        onSelectNote = notesViewModel::selectNote,
    )
}

@Composable
fun NotesScreenContent(
    modifier: Modifier = Modifier,
    selectedNotePath: String?,
    isEditing: StateAndEvent<Boolean>,
    editableContent: StateAndEvent<String>,
    selectedNote: Persisted<Note>?,
    notes: List<Persisted<Note>>,
    onCreateNote: () -> Unit,
    onSaveNote: () -> Unit,
    onDeleteNote: () -> Unit,
    onSelectNote: (String) -> Unit
) {
    //TODO if this is a full screen you should consider using Scaffold

    // TODO if modifier is passed from outside, do not override it
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(getText("notes.title"), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // TODO should it be variable?
            val masterWeight = if (maxWidth < 640.dp) 0.55f else 0.33f
            TwoPaneLayout(
                modifier = Modifier.fillMaxSize(),
                masterWeight = masterWeight,
                master = {
                    SelectableList(
                        items = notes,
                        selectedKey = selectedNotePath,
                        modifier = Modifier.fillMaxSize(),
                        keyOf = { it.file.canonicalPath },
                        onItemClick = { note ->
                            onSelectNote(note.file.canonicalPath)
                        }
                    ) { note, _ ->
                        Text(
                            // TODO if one parameter has named argument, all should have
                            note.value.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        // TODO Integer goes to resources
                        SimpleDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                },
                detail = {
                    DetailEditorPane(
                        modifier = Modifier.fillMaxSize(),
                        // TODO Integer goes to resources
                        verticalSpacing = 8.dp,
                        header = {
                            DetailHeader(
                                title = selectedNote?.value?.title ?: getText("notes.empty.message"),
                                actions = {
                                    CrudActionBar(
                                        hasSelection = selectedNote != null,
                                        isEditing = isEditing.state,
                                        onNew = onCreateNote,
                                        onEdit = { isEditing.event(true) },
                                        onSave = onSaveNote,
                                        onDelete = onDeleteNote,
                                        labels = CrudActionLabels(
                                            newLabel = getText("notes.new"),
                                            editLabel = getText("crud.edit"),
                                            saveLabel = getText("crud.save"),
                                            deleteLabel = getText("crud.delete")
                                        )
                                    )
                                }
                            )
                        },
                        isEditing = isEditing.state,
                        editContent = {
                            TextField(
                                value = editableContent.state,
                                onValueChange = editableContent.event,
                                modifier = Modifier.fillMaxSize()
                            )
                        },
                        viewContent = {
                            selectedNote?.let { note ->
                                SelectionContainer(
                                    modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                                ) {
                                    Markdown(note.value.content)
                                }
                            } ?: EmptyDetailHint(
                                message = getText("notes.empty.message"),
                                description = getText("notes.empty.description")
                            )
                        }
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewLight() {
    val firstNote = previewPersistedNote("first-note.md", 1, "First note", "# Preview\nThis is the first note.")
    val secondNote = previewPersistedNote("second-note.md", 2, "Second note", "Second entry markdown content.")

    KoinApplicationPreview(application = {
        fileProperties("/koinLocal.properties")
        fileProperties("/strings.properties")
        modules(mainModule, localModule, plainLocalModule)
    }) {
        AppTheme(darkTheme = false) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                NotesScreenContent(
                    notes = listOf(firstNote, secondNote),
                    selectedNotePath = firstNote.file.absolutePath,
                    selectedNote = firstNote,
                    editableContent = StateAndEvent(
                        state = firstNote.value.content,
                        event = {}
                    ),
                    isEditing = StateAndEvent(
                        state = true,
                        event = {}
                    ),
                    onCreateNote = {},
                    onSaveNote = {},
                    onDeleteNote = {},
                    onSelectNote = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewDark() {
    val firstNote = previewPersistedNote("first-note.md", 1, "First note", "# Preview\nThis is the first note.")
    val secondNote = previewPersistedNote("second-note.md", 2, "Second note", "Second entry markdown content.")

    KoinApplicationPreview(application = {
        fileProperties("/koinLocal.properties")
        fileProperties("/strings.properties")
        modules(mainModule, localModule, plainLocalModule)
    }) {
        AppTheme(darkTheme = true) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                NotesScreenContent(
                    notes = listOf(firstNote, secondNote),
                    selectedNotePath = firstNote.file.absolutePath,
                    selectedNote = firstNote,
                    editableContent = StateAndEvent(
                        state = firstNote.value.content,
                        event = {}
                    ),
                    isEditing = StateAndEvent(
                        state = true,
                        event = {}
                    ),
                    onCreateNote = {},
                    onSaveNote = {},
                    onDeleteNote = {},
                    onSelectNote = {},
                )
            }
        }
    }
}

private fun previewPersistedNote(name: String, id: Int, title: String, content: String): Persisted<Note> {
    val file = File(name).absoluteFile
    return Persisted(file, Note(id, title, content))
}
