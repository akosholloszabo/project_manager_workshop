package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hu.akosholloszabo.project_manager.project_manager_workshop.component.RenderMarkdown
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.File

@Composable
fun NotesScreenContent() {
    val sampleContent = File("../samples/sample_note.md").readText()

    val notesState = remember {
        mutableStateListOf(
            Note(1, "Meeting notes", sampleContent),
            Note(2, "Ideas", "# Ideas\n- New feature X..."),
            Note(3, "Draft", "# Draft\nThis is a draft note.")
        )
    }

    var selectedNoteId by rememberSaveable { mutableStateOf(if (notesState.isNotEmpty()) notesState[0].id else null) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editableContent by rememberSaveable { mutableStateOf("") }

    val selectedNote = notesState.find { it.id == selectedNoteId }
    if (!isEditing && selectedNote != null && editableContent != selectedNote.content) {
        editableContent = selectedNote.content
    }

    fun saveNote(noteId: Int?) {
        if (noteId == null) return
        val idx = notesState.indexOfFirst { it.id == noteId }
        if (idx >= 0) {
            val old = notesState[idx]
            if (old.content != editableContent) notesState[idx] = old.copy(content = editableContent)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: titles only
            Column(modifier = Modifier.width(320.dp).fillMaxSize()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(notesState) { _, note ->
                        val isSelected = note.id == selectedNoteId
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable {
                                    // If we are editing, save changes for the current note (if any) before switching
                                    if (isEditing && selectedNoteId != null) {
                                        saveNote(selectedNoteId)
                                    }
                                    // Always leave edit mode when switching notes
                                    isEditing = false

                                    // Update selection and load the new note's content
                                    selectedNoteId = note.id
                                    editableContent = note.content
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                note.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            SimpleDivider(
                                modifier = Modifier.padding(
                                    top = 8.dp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: viewer / editor
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        selectedNote?.title ?: "No note selected",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (selectedNote != null) {
                        if (isEditing) {
                            Button(onClick = { saveNote(selectedNoteId); isEditing = false }) { Text("Save") }
                        } else {
                            Button(onClick = { isEditing = true }) { Text("Edit") }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (selectedNote == null) {
                    Text(
                        "Select a note to view or edit it.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    if (isEditing) {
                        TextField(
                            value = editableContent,
                            onValueChange = { editableContent = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        RenderMarkdown(selectedNote.content)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun NotesPreviewLight() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            NotesScreenContent()
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun NotesPreviewDark() {
    hu.akosholloszabo.project_manager.project_manager_workshop.AppTheme(darkTheme = true) {
        Surface(color = Color(0xFF121212), modifier = Modifier.fillMaxSize()) {
            NotesScreenContent()
        }
    }
}
