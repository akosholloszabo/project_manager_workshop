package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteStore(
    private val workingFolder: String?,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _notes = MutableStateFlow<List<Persisted<Note>>>(emptyList())
    val notes: StateFlow<List<Persisted<Note>>> = _notes.asStateFlow()

    init {
        refreshNotes()
    }

    fun refreshNotes() {
        scope.launch {
            val loaded = NotesStorage.loadNotes(workingFolder)
            _notes.emit(loaded)
        }
    }

    //TODO do it reactive instead of returning it directly
    suspend fun createNote(title: String? = null, content: String = ""): Persisted<Note>? {
        val created = withContext(Dispatchers.IO) {
            NotesStorage.createNote(workingFolder, title, content)
        }
        if (created != null) {
            refreshNotes()
        }
        return created
    }

    suspend fun saveNote(note: Persisted<Note>, content: String): Boolean {
        val success = withContext(Dispatchers.IO) {
            NotesStorage.saveNoteContent(note.file, content)
        }
        if (success) {
            refreshNotes()
        }
        return success
    }

    suspend fun deleteNote(note: Persisted<Note>): Boolean {
        val success = withContext(Dispatchers.IO) {
            NotesStorage.deleteNote(note.file)
        }
        if (success) {
            refreshNotes()
        }
        return success
    }
}

