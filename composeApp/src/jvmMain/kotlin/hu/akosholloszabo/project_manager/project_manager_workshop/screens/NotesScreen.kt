package hu.akosholloszabo.project_manager.project_manager_workshop.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.NotesScreenState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
fun NotesScreen(workingFolder: String) {
    val notesViewModel = remember(workingFolder) {
        NotesViewModel(NoteStore(workingFolder))
    }
    LaunchedEffect(workingFolder) {
        notesViewModel.refresh()
    }
    val uiState by notesViewModel.uiState.collectAsState()
    NotesScreenContent(
        uiState = uiState,
        onCreateNote = notesViewModel::createNote,
        onStartEditing = notesViewModel::startEditing,
        onSaveNote = notesViewModel::saveNote,
        onDeleteNote = notesViewModel::deleteNote,
        onSelectNote = notesViewModel::selectNote,
        onContentChange = notesViewModel::updateContent
    )
}

@Composable
fun NotesScreenContent(
    uiState: NotesScreenState,
    onCreateNote: () -> Unit,
    onStartEditing: () -> Unit,
    onSaveNote: () -> Unit,
    onDeleteNote: () -> Unit,
    onSelectNote: (String) -> Unit,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            masterWeight = 0.33f,
            master = {
                SelectableList(
                    items = uiState.notes,
                    selectedKey = uiState.selectedNotePath,
                    modifier = Modifier.fillMaxSize(),
                    keyOf = { it.file.canonicalPath },
                    onItemClick = { note -> onSelectNote(note.file.canonicalPath) }
                ) { note, _ ->
                    Text(
                        note.value.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    SimpleDivider(modifier = Modifier.padding(top = 8.dp))
                }
            },
            detail = {
                DetailEditorPane(
                    modifier = Modifier.fillMaxSize(),
                    verticalSpacing = 8.dp,
                    header = {
                        DetailHeader(
                            title = uiState.selectedNote?.value?.title ?: "No note selected",
                            actions = {
                                CrudActionBar(
                                    hasSelection = uiState.selectedNote != null,
                                    isEditing = uiState.isEditing,
                                    onNew = onCreateNote,
                                    onEdit = onStartEditing,
                                    onSave = onSaveNote,
                                    onDelete = onDeleteNote,
                                    labels = CrudActionLabels(newLabel = "New note")
                                )
                            }
                        )
                    },
                    isEditing = uiState.isEditing,
                    editContent = {
                        TextField(
                            value = uiState.editableContent,
                            onValueChange = onContentChange,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    viewContent = {
                        uiState.selectedNote?.let { note ->
                            SelectionContainer(
                                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                            ) {
                                Markdown(note.value.content)
                            }
                        } ?: EmptyDetailHint("Select a note to view or edit it.")
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewLight() {
    AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            NotesScreenContent(
                uiState = previewNotesState(),
                onCreateNote = {},
                onStartEditing = {},
                onSaveNote = {},
                onDeleteNote = {},
                onSelectNote = {},
                onContentChange = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            NotesScreenContent(
                uiState = previewNotesState(),
                onCreateNote = {},
                onStartEditing = {},
                onSaveNote = {},
                onDeleteNote = {},
                onSelectNote = {},
                onContentChange = {}
            )
        }
    }
}

private fun previewPersistedNote(name: String, id: Int, title: String, content: String): Persisted<Note> {
    val file = File(name).absoluteFile
    return Persisted(file, Note(id, title, content))
}

private fun previewNotesState(): NotesScreenState {
    val firstNote = previewPersistedNote("first-note.md", 1, "First note", "# Preview\nThis is the first note.")
    val secondNote = previewPersistedNote("second-note.md", 2, "Second note", "Second entry markdown content.")
    return NotesScreenState(
        notes = listOf(firstNote, secondNote),
        selectedNotePath = firstNote.file.absolutePath,
        selectedNote = firstNote,
        editableContent = firstNote.value.content
    )
}
