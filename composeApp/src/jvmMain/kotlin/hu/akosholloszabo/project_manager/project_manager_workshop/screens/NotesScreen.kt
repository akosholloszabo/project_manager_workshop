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
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotesScreenContent() {
    // Sample in-memory notes; in a real app these would come from a ViewModel/repository
    val notesState = remember {
        mutableStateListOf(
            Note(1, "Meeting notes", "# Meeting\n- Items to discuss..."),
            Note(2, "Ideas", "# Ideas\n- New feature X..."),
            Note(3, "Draft", "# Draft\nThis is a draft note.")
        )
    }

    // Selected note id (default to first note if available)
    var selectedNoteId by rememberSaveable { mutableStateOf(if (notesState.isNotEmpty()) notesState[0].id else null) }

    // Editing state and editable content buffer
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editableContent by rememberSaveable { mutableStateOf("") }

    // Initialize editableContent to the selected note's content when first shown
    val selectedNote = notesState.find { it.id == selectedNoteId }
    if (!isEditing && selectedNote != null && editableContent != selectedNote.content) {
        // keep editableContent in sync when not editing (e.g., on first paint or after save)
        editableContent = selectedNote.content
    }

    fun saveNote(noteId: Int?) {
        if (noteId == null) return
        val index = notesState.indexOfFirst { it.id == noteId }
        if (index >= 0) {
            val old = notesState[index]
            if (old.content != editableContent) {
                notesState[index] = old.copy(content = editableContent)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            // Left: notes list
            Column(modifier = Modifier.width(320.dp).fillMaxSize()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(notesState) { _, note ->
                        val isSelected = note.id == selectedNoteId
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable {
                                    // Auto-save current if editing and dirty
                                    if (isEditing && selectedNoteId != null && editableContent != notesState.find { it.id == selectedNoteId }?.content) {
                                        saveNote(selectedNoteId)
                                        isEditing = false
                                    }
                                    // Switch selection
                                    selectedNoteId = note.id
                                    // Load the selected note's content into editor buffer
                                    editableContent = note.content
                                }
                                .padding(8.dp)
                        ) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(note.content.take(120) + if (note.content.length > 120) "..." else "")
                            hu.akosholloszabo.project_manager.project_manager_workshop.SimpleDivider(
                                modifier = Modifier.padding(
                                    top = 8.dp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right: selected note viewer / editor
            Column(modifier = Modifier.fillMaxSize()) {
                // Top row with title and Edit/Save button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(selectedNote?.title ?: "No note selected", style = MaterialTheme.typography.titleLarge)
                    if (selectedNote != null) {
                        if (isEditing) {
                            Button(onClick = {
                                // Save and exit editing
                                saveNote(selectedNoteId)
                                isEditing = false
                            }) {
                                Text("Save")
                            }
                        } else {
                            Button(onClick = {
                                isEditing = true
                            }) {
                                Text("Edit")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Content area
                if (selectedNote == null) {
                    Text("Select a note to view or edit it.", style = MaterialTheme.typography.bodyLarge)
                } else {
                    if (isEditing) {
                        // Editable text field
                        TextField(
                            value = editableContent,
                            onValueChange = { editableContent = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Rendered view (simple fallback: show raw markdown as text)
                        // If you add a markdown renderer later, replace this with that renderer
                        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                            Text(editableContent, style = MaterialTheme.typography.bodyLarge)
                        }
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
