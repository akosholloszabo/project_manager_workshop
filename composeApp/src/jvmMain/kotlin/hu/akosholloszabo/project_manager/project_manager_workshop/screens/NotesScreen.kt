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
import hu.akosholloszabo.project_manager.project_manager_workshop.actions.CrudAction
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionBar
import hu.akosholloszabo.project_manager.project_manager_workshop.component.CrudActionLabels
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailEditorPane
import hu.akosholloszabo.project_manager.project_manager_workshop.component.DetailHeader
import hu.akosholloszabo.project_manager.project_manager_workshop.component.EmptyDetailHint
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SelectableList
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.component.TwoPaneLayout
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotesScreenContent(workingFolder: String) {
    val notesViewModel = remember(workingFolder) {
        NotesViewModel(NoteStore(workingFolder))
    }
    LaunchedEffect(workingFolder) {
        notesViewModel.refresh()
    }
    val uiState by notesViewModel.uiState.collectAsState()

    fun handleAction(action: CrudAction) {
        when (action) {
            CrudAction.Create -> notesViewModel.createNote()
            CrudAction.Edit -> notesViewModel.startEditing()
            CrudAction.Save -> notesViewModel.saveNote()
            CrudAction.Delete -> notesViewModel.deleteNote()
        }
    }

    val notes = uiState.notes
    val selectedNote = uiState.selectedNote
    val selectedNotePath = uiState.selectedNotePath
    val isEditing = uiState.isEditing

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        TwoPaneLayout(
            modifier = Modifier.fillMaxSize(),
            masterWeight = 0.33f,
            master = {
                SelectableList(
                    items = notes,
                    selectedKey = selectedNotePath,
                    modifier = Modifier.fillMaxSize(),
                    keyOf = { it.file.canonicalPath },
                    onItemClick = { note ->
                        val targetPath = note.file.canonicalPath
                        if (isEditing && selectedNote?.file?.canonicalPath != targetPath) {
                            notesViewModel.saveNote()
                        }
                        notesViewModel.selectNote(targetPath)
                    }
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
                            title = selectedNote?.value?.title ?: "No note selected",
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
                        TextField(
                            value = uiState.editableContent,
                            onValueChange = notesViewModel::updateContent,
                            modifier = Modifier.fillMaxSize()
                        )
                    },
                    viewContent = {
                        if (selectedNote == null) {
                            EmptyDetailHint(message = "Select a note to view or edit it.")
                        } else {
                            SelectionContainer(
                                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                            ) {
                                Markdown(selectedNote.value.content)
                            }
                        }
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
