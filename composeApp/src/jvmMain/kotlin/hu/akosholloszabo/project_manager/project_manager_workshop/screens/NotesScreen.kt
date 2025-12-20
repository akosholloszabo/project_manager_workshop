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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme
import hu.akosholloszabo.project_manager.project_manager_workshop.actions.CrudAction
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotesScreenContent(workingFolder: String) {
    val notesState = remember { mutableStateListOf<NotesStorage.PersistedNote>() }
    var selectedNotePath by rememberSaveable { mutableStateOf<String?>(null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editableContent by rememberSaveable { mutableStateOf("") }

    fun refreshNotes(preservePath: String? = null) {
        val loaded = NotesStorage.loadNotes(workingFolder)
        notesState.apply {
            clear()
            addAll(loaded)
        }
        selectedNotePath = when {
            preservePath != null && loaded.any { it.file.canonicalPath == preservePath } -> preservePath
            loaded.isNotEmpty() -> loaded[0].file.canonicalPath
            else -> null
        }
    }

    LaunchedEffect(workingFolder) {
        isEditing = false
        refreshNotes()
    }

    val selectedNote by remember(notesState, selectedNotePath) {
        derivedStateOf {
            selectedNotePath?.let { path ->
                notesState.firstOrNull { it.file.canonicalPath == path }
            }
        }
    }

    LaunchedEffect(selectedNote?.file?.canonicalPath, isEditing) {
        if (!isEditing) {
            editableContent = selectedNote?.note?.content ?: ""
        }
    }

    fun handleAction(action: CrudAction) {
        when (action) {
            CrudAction.Create -> {
                val created = NotesStorage.createNote(workingFolder) ?: return
                isEditing = true
                refreshNotes(preservePath = created.file.canonicalPath)
                editableContent = created.note.content
            }

            CrudAction.Edit -> if (selectedNote != null) {
                isEditing = true
            }

            CrudAction.Save -> {
                val current = selectedNote ?: return
                if (NotesStorage.saveNoteContent(current.file, editableContent)) {
                    isEditing = false
                    refreshNotes(preservePath = current.file.canonicalPath)
                }
            }

            CrudAction.Delete -> {
                val current = selectedNote ?: return
                if (NotesStorage.deleteNote(current.file)) {
                    isEditing = false
                    refreshNotes()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            masterWeight = 0.33f,
            master = {
                NotesSidebar(
                    notes = notesState,
                    selectedPath = selectedNotePath,
                    modifier = Modifier.fillMaxSize()
                ) { note ->
                    val targetPath = note.file.canonicalPath
                    if (isEditing && selectedNote?.file?.canonicalPath != targetPath) {
                        handleAction(CrudAction.Save)
                    }
                    isEditing = false
                    selectedNotePath = targetPath
                }
            },
            detail = {
                DetailEditorPane(
                    modifier = Modifier.fillMaxSize(),
                    verticalSpacing = 8.dp,
                    header = {
                        DetailHeader(
                            title = selectedNote?.note?.title ?: "No note selected",
                            actions = {
                                CrudActionBar(
                                    hasSelection = selectedNote != null,
                                    isEditing = isEditing,
                                    onNew = { handleAction(CrudAction.Create) },
                                    onEdit = { handleAction(CrudAction.Edit) },
                                    onSave = { handleAction(CrudAction.Save) },
                                    onDelete = { handleAction(CrudAction.Delete) },
                                    labels = CrudActionLabels(newLabel = "New note")
                                )
                            }
                        )
                    },
                    isEditing = isEditing,
                    editContent = {
                        NoteEditorContent(editableContent, onContentChange = { editableContent = it })
                    },
                    viewContent = {
                        NoteViewerContent(note = selectedNote)
                    }
                )
            }
        )
    }
}

@Composable
private fun NotesSidebar(
    notes: List<NotesStorage.PersistedNote>,
    selectedPath: String?,
    modifier: Modifier = Modifier,
    onSelectNote: (NotesStorage.PersistedNote) -> Unit
) {
    SelectableList(
        items = notes,
        selectedKey = selectedPath,
        modifier = modifier,
        keyOf = { it.file.canonicalPath },
        onItemClick = onSelectNote
    ) { note, _ ->
        Text(
            note.note.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        SimpleDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun NoteEditorContent(content: String, onContentChange: (String) -> Unit) {
    TextField(
        value = content,
        onValueChange = onContentChange,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun NoteViewerContent(note: NotesStorage.PersistedNote?) {
    if (note == null) {
        EmptyDetailHint(message = "Select a note to view or edit it.")
    } else {
        SelectionContainer(
            modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
        ) {
            Markdown(note.note.content)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewLight() {
    AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            NotesScreenContent(workingFolder = "preview")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreviewDark() {
    AppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            NotesScreenContent(workingFolder = "preview")
        }
    }
}
