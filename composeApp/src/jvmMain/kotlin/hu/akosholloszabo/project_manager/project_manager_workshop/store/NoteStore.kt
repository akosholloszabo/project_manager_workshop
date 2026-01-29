package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NoteStore(
    private val workingFolderStore: WorkingFolderStore?,
    private val notesStorage: NotesStorage
) {
    private val _notes = MutableStateFlow<List<Persisted<Note>>>(emptyList())
    val notes: StateFlow<List<Persisted<Note>>> = _notes.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            workingFolderStore?.session?.let { sessionFlow ->
                sessionFlow.collectLatest { session ->
                    refreshNotes(session)
                }
            } ?: refreshNotes(null)
        }
    }

    fun refreshNotes(session: StorageSession? = workingFolderStore?.session?.value) =
        _notes.tryEmit(notesStorage.loadNotes(session))

    fun createNote(title: String? = null, content: String = ""): Persisted<Note>? =
        notesStorage.createNote(workingFolderStore?.session?.value, title, content)
            ?.also { refreshNotes() }

    fun saveNote(note: Persisted<Note>, content: String): Boolean =
        notesStorage.saveNoteContent(workingFolderStore?.session?.value, note.file, content)
            .also { success ->
                if (success) {
                    refreshNotes()
                }
            }

    fun deleteNote(note: Persisted<Note>): Boolean =
        notesStorage.deleteNote(workingFolderStore?.session?.value, note.file)
            .also { success ->
                if (success) {
                    refreshNotes()
                }
            }
}
