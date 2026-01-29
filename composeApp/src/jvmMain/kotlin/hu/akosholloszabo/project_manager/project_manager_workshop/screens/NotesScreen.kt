package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.model.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_delete
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_edit
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.crud_save
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.notes_empty_description
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.notes_empty_message
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.notes_new
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.notes_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.PreviewWrapper
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.StateAndEvent
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
fun NotesScreen(notesViewModel: NotesViewModel) {
    val selectedNotePath by notesViewModel.selectedNotePath.collectAsStateWithLifecycle()
    val isEditing by notesViewModel.isEditing.collectAsStateWithLifecycle()
    val editableContent by notesViewModel.editableContent.collectAsStateWithLifecycle()
    val selectedNote by notesViewModel.selectedNote.collectAsStateWithLifecycle()
    val notes by notesViewModel.notes.collectAsStateWithLifecycle()

    LaunchedEffect(notesViewModel) {
        notesViewModel.refresh()
    }

    NotesScreenContent(
        selectedNotePath = selectedNotePath,
        isEditing = StateAndEvent(
            value = isEditing,
            event = notesViewModel::setEditing
        ),
        editableContent = StateAndEvent(
            value = editableContent,
            event = notesViewModel::updateEditableContent
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

    Scaffold(modifier) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(stringResource(Res.string.notes_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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
                                text = note.value.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                        }
                    },
                    detail = {
                        DetailEditorPane(
                            modifier = Modifier.fillMaxSize(),
                            verticalSpacing = 8.dp,
                            header = {
                                DetailHeader(
                                    title = selectedNote?.value?.title
                                        ?: stringResource(Res.string.notes_empty_message),
                                    actions = {
                                        CrudActionBar(
                                            hasSelection = selectedNote != null,
                                            isEditing = isEditing.value,
                                            onNew = onCreateNote,
                                            onEdit = { isEditing.event(true) },
                                            onSave = onSaveNote,
                                            onDelete = onDeleteNote,
                                            labels = CrudActionLabels(
                                                newLabel = stringResource(Res.string.notes_new),
                                                editLabel = stringResource(Res.string.crud_edit),
                                                saveLabel = stringResource(Res.string.crud_save),
                                                deleteLabel = stringResource(Res.string.crud_delete)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                )
                            },
                            isEditing = isEditing.value,
                            editContent = {
                                TextField(
                                    value = editableContent.value,
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
                                    message = stringResource(Res.string.notes_empty_message),
                                    description = stringResource(Res.string.notes_empty_description)
                                )
                            }
                        )
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewLight() {
    val firstNote = previewPersistedNote("first-note.md", 1, "First note", "# Preview\nThis is the first note.")
    val secondNote = previewPersistedNote("second-note.md", 2, "Second note", "Second entry markdown content.")
    PreviewWrapper(darkTheme = false) {
        NotesScreenContent(
            notes = listOf(firstNote, secondNote),
            selectedNotePath = firstNote.file.absolutePath,
            selectedNote = firstNote,
            editableContent = StateAndEvent(
                value = firstNote.value.content,
                event = {}
            ),
            isEditing = StateAndEvent(
                value = true,
                event = {}
            ),
            onCreateNote = {},
            onSaveNote = {},
            onDeleteNote = {},
            onSelectNote = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewDark() {
    val firstNote = previewPersistedNote("first-note.md", 1, "First note", "# Preview\nThis is the first note.")
    val secondNote = previewPersistedNote("second-note.md", 2, "Second note", "Second entry markdown content.")

    PreviewWrapper(darkTheme = true) {
        NotesScreenContent(
            notes = listOf(firstNote, secondNote),
            selectedNotePath = firstNote.file.absolutePath,
            selectedNote = firstNote,
            editableContent = StateAndEvent(
                value = firstNote.value.content,
                event = {}
            ),
            isEditing = StateAndEvent(
                value = true,
                event = {}
            ),
            onCreateNote = {},
            onSaveNote = {},
            onDeleteNote = {},
            onSelectNote = {},
        )
    }
}

private fun previewPersistedNote(name: String, id: Int, title: String, content: String): Persisted<Note> {
    val file = File(name).absoluteFile
    return Persisted(file, Note(id, title, content))
}
