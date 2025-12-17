package hu.akosholloszabo.project_manager.project_manager_workshop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import hu.akosholloszabo.project_manager.project_manager_workshop.component.SimpleDivider
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NotesScreenContent(workingFolder: String? = null) {
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

    val selectedNote = selectedNotePath?.let { path ->
        notesState.find { it.file.canonicalPath == path }
    }

    LaunchedEffect(selectedNote?.file?.canonicalPath, isEditing) {
        if (!isEditing) {
            editableContent = selectedNote?.note?.content ?: ""
        }
    }

    fun createNewNote() {
        val created = NotesStorage.createNote(workingFolder) ?: return
        isEditing = true
        refreshNotes(preservePath = created.file.canonicalPath)
        editableContent = created.note.content
    }

    fun saveCurrentNote() {
        val current = selectedNote ?: return
        if (NotesStorage.saveNoteContent(current.file, editableContent)) {
            isEditing = false
            refreshNotes(preservePath = current.file.canonicalPath)
        }
    }

    fun deleteCurrentNote() {
        val current = selectedNote ?: return
        if (NotesStorage.deleteNote(current.file)) {
            isEditing = false
            refreshNotes()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notes", style = MaterialTheme.typography.titleLarge)
        Text(
            workingFolder?.let { "Working folder: $it" } ?: "Working folder not set",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxSize()) {
            // Left: titles only
            Column(modifier = Modifier.width(320.dp).fillMaxSize()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(notesState) { _, note ->
                        val isSelected = note.file.canonicalPath == selectedNotePath
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                                .clickable {
                                    if (isEditing && selectedNote != null) {
                                        saveCurrentNote()
                                    }
                                    isEditing = false
                                    selectedNotePath = note.file.canonicalPath
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                note.note.title,
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
                        selectedNote?.note?.title ?: "No note selected",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { createNewNote() },
                            enabled = workingFolder != null
                        ) {
                            Text("New note")
                        }
                        selectedNote?.let {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (isEditing) {
                                    Button(onClick = { saveCurrentNote() }) { Text("Save") }
                                } else {
                                    Button(onClick = { isEditing = true }) { Text("Edit") }
                                }
                                Button(onClick = { deleteCurrentNote() }) { Text("Delete") }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Box(modifier = Modifier.fillMaxSize()) {
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
                            SelectionContainer(
                                modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxSize()
                            ) {
                                Markdown(selectedNote.note.content)
                            }
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
