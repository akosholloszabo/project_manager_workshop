package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteStore(
    private val workingFolderStore: WorkingFolderStore,
    private val notesStorage: NotesStorage
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _notes = MutableStateFlow<List<Persisted<Note>>>(emptyList())
    val notes: StateFlow<List<Persisted<Note>>> = _notes.asStateFlow()

    init {
        scope.launch {
            workingFolderStore.session.collectLatest { session ->
                refreshNotesInternal(session)
            }
        }
    }

    fun refreshNotes() {
        scope.launch {
            refreshNotesInternal(workingFolderStore.session.value)
        }
    }

    private suspend fun refreshNotesInternal(session: StorageSession?) {
        val loaded = withContext(Dispatchers.IO) {
            notesStorage.loadNotes(session)
        }
        _notes.emit(loaded)
    }

    suspend fun createNote(title: String? = null, content: String = ""): Persisted<Note>? {
        val created = withContext(Dispatchers.IO) {
            notesStorage.createNote(workingFolderStore.session.value, title, content)
        }
        if (created != null) {
            refreshNotes()
        }
        return created
    }

    suspend fun saveNote(note: Persisted<Note>, content: String): Boolean {
        val success = withContext(Dispatchers.IO) {
            notesStorage.saveNoteContent(workingFolderStore.session.value, note.file, content)
        }
        if (success) {
            refreshNotes()
        }
        return success
    }

    suspend fun deleteNote(note: Persisted<Note>): Boolean {
        val success = withContext(Dispatchers.IO) {
            notesStorage.deleteNote(workingFolderStore.session.value, note.file)
        }
        if (success) {
            refreshNotes()
        }
        return success
    }
}
