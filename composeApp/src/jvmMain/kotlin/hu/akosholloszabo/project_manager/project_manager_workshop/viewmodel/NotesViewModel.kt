package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val noteStore: NoteStore
) : ViewModel() {
    private val _selectedNotePath = MutableStateFlow<String?>(null)
    val selectedNotePath: StateFlow<String?> = _selectedNotePath.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editableContent = MutableStateFlow("")
    val editableContent: StateFlow<String> = _editableContent.asStateFlow()

    val notes = noteStore.notes

    val selectedNote: StateFlow<Persisted<Note>?> = combine(
        noteStore.notes,
        selectedNotePath
    ) { notes, path ->
        notes.findByPath(path)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            noteStore.notes.collect { currentNotes ->
                val nextPath = currentNotes.determineSelection(selectedNotePath.value)
                if (nextPath != selectedNotePath.value) {
                    updateSelection(path = nextPath, notes = currentNotes, refreshContent = !isEditing.value)
                }
            }
        }
    }

    fun refresh() = noteStore.refreshNotes()

    fun createNote() = viewModelScope.launch {
        noteStore.createNote()?.let { created ->
            _selectedNotePath.tryEmit(created.file.canonicalPath)
            _editableContent.tryEmit(created.value.content)
            _isEditing.tryEmit(true)
        }
    }

    fun saveNote() = viewModelScope.launch {
        val note = selectedNote.value ?: return@launch
        val content = editableContent.value
        if (noteStore.saveNote(note, content)) {
            stopEditing(content)
        }
    }

    fun deleteNote() = viewModelScope.launch {
        selectedNote.value?.takeIf { noteStore.deleteNote(it) }?.let {
            _selectedNotePath.tryEmit(null)
            stopEditing("")
        }
    }

    fun selectNote(path: String) = viewModelScope.launch {
        if (selectedNotePath.value == path) return@launch
        if (!saveIfEditing()) return@launch
        updateSelection(path = path, notes = notes.value, refreshContent = true)
    }

    fun setEditing(isEditing: Boolean) = _isEditing.tryEmit(isEditing)

    fun updateEditableContent(content: String) = _editableContent.tryEmit(content)

    private fun saveIfEditing(): Boolean =
        if (!isEditing.value) {
            true
        } else run {
            val content = editableContent.value
            val saved = selectedNote.value?.let { noteStore.saveNote(it, content) } ?: true
            if (saved) {
                stopEditing(content)
            }
            saved
        }

    private fun updateSelection(
        path: String?,
        notes: List<Persisted<Note>>,
        refreshContent: Boolean
    ) {
        _selectedNotePath.tryEmit(path)
        if (!isEditing.value && refreshContent) {
            _editableContent.tryEmit(notes.findByPath(path)?.value?.content ?: "")
        }
    }

    private fun stopEditing(savedContent: String? = null) {
        _isEditing.tryEmit(false)
        _editableContent.tryEmit(
            savedContent ?: selectedNote.value?.value?.content ?: ""
        )
    }

    fun List<Persisted<Note>>.findByPath(path: String?): Persisted<Note>? =
        path?.let { requested -> firstOrNull { it.file.canonicalPath == requested } }

    fun List<Persisted<Note>>.determineSelection(currentPath: String?): String? = when {
        currentPath != null && any { it.file.canonicalPath == currentPath } -> currentPath
        isNotEmpty() -> first().file.canonicalPath
        else -> null
    }
}
