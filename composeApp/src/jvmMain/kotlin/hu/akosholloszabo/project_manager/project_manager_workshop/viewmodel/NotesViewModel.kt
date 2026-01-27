package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val noteStore: NoteStore
) { //TODO Why is this not extends form a ViewModel()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _selectedNotePath = MutableStateFlow<String?>(null)
    val selectedNotePath: StateFlow<String?> = _selectedNotePath.asStateFlow()

    //TODO should this be private
    val isEditing = MutableStateFlow(false)

    // TODO should this be private
    val editableContent = MutableStateFlow("")

    val notes = noteStore.notes

    val selectedNote: StateFlow<Persisted<Note>?> = combine(
        noteStore.notes,
        _selectedNotePath
    ) { notes, path ->
        notes.findByPath(path)
    }.stateIn(scope, SharingStarted.Eagerly, null)


    init {
        scope.launch {
            noteStore.notes.collect { currentNotes ->
                // TODO you should check the non "_" version
                val nextPath = currentNotes.determineSelection(_selectedNotePath.value)
                // TODO you should check the non "_" version
                if (nextPath != _selectedNotePath.value) {
                    // TODO If one parameter is named, all should be named
                    updateSelection(nextPath, currentNotes, refreshContent = !isEditing.value)
                }
            }
        }
    }

    fun refresh() = noteStore.refreshNotes()

    // TODO what is this?
    fun createNote() = scope.launch {
        noteStore.createNote()?.let { created ->
            _selectedNotePath.tryEmit(created.file.canonicalPath)
            editableContent.tryEmit(created.value.content)
            isEditing.tryEmit(true)
        }
    }

    fun saveNote() = scope.launch {
        val note = selectedNote.value ?: return@launch
        val content = editableContent.value
        if (noteStore.saveNote(note, content)) {
            stopEditing(content)
        }
    }

    fun deleteNote() = scope.launch {
        selectedNote.value?.takeIf { noteStore.deleteNote(it) }?.let {
            _selectedNotePath.tryEmit(null)
            stopEditing("")
        }
    }

    fun selectNote(path: String) = scope.launch {
        // TODO you should check the non "_" version
        if (_selectedNotePath.value == path) return@launch
        if (!saveIfEditing()) return@launch
        // TODO If one parameter is named, all should be named
        updateSelection(path, notes.value, refreshContent = true)
    }

    private suspend fun saveIfEditing(): Boolean {
        if (!isEditing.value) return true
        val content = editableContent.value
        val saved = selectedNote.value?.let { noteStore.saveNote(it, content) } ?: true
        if (saved) {
            stopEditing(content)
        }
        return saved
    }

    private fun updateSelection(
        // TODO should it be nullable
        path: String?,
        notes: List<Persisted<Note>>,
        refreshContent: Boolean
    ) {
        _selectedNotePath.tryEmit(path)
        if (!isEditing.value && refreshContent) {
            editableContent.tryEmit(notes.findByPath(path)?.value?.content ?: "")
        }
    }

    private fun stopEditing(savedContent: String? = null) {
        isEditing.tryEmit(false)
        editableContent.tryEmit(
            savedContent ?: selectedNote.value?.value?.content ?: ""
        )
    }

    //TODO this two should be in an extension file
    private fun List<Persisted<Note>>.findByPath(path: String?): Persisted<Note>? =
        path?.let { requested ->
            firstOrNull { it.file.canonicalPath == requested }
        }

    private fun List<Persisted<Note>>.determineSelection(currentPath: String?): String? =
        when {
            currentPath != null && any { it.file.canonicalPath == currentPath } -> currentPath
            isNotEmpty() -> first().file.canonicalPath
            else -> null
        }
}
