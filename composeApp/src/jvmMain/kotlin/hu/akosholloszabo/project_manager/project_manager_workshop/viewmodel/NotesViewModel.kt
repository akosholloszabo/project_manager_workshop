package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.NotesScreenState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    private val noteStore: NoteStore,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    private val _selectedNotePath = MutableStateFlow<String?>(null)
    private val _isEditing = MutableStateFlow(false)
    private val _editableContent = MutableStateFlow("")

    val uiState: StateFlow<NotesScreenState> = combine(
        noteStore.notes,
        _selectedNotePath,
        _isEditing,
        _editableContent
    ) { notes, path, editing, editableContent ->
        val selectedNote = path?.let { target ->
            notes.firstOrNull { it.file.canonicalPath == target }
        }
        NotesScreenState(
            notes = notes,
            selectedNotePath = path,
            selectedNote = selectedNote,
            isEditing = editing,
            editableContent = editableContent
        )
    }.stateIn(scope, SharingStarted.Eagerly, NotesScreenState())

    init {
        scope.launch {
            noteStore.notes.collect { notes ->
                val nextPath = determineSelection(notes)
                if (nextPath != _selectedNotePath.value) {
                    setSelectedNotePathInternal(nextPath, notes, refreshContent = !_isEditing.value)
                }
            }
        }
    }

    fun refresh() {
        noteStore.refresh()
    }

    fun createNote() {
        scope.launch {
            val created = noteStore.createNote()
            if (created != null) {
                _selectedNotePath.value = created.file.canonicalPath
                _editableContent.value = created.value.content
                _isEditing.value = true
            }
        }
    }

    fun startEditing() {
        _isEditing.value = true
    }

    fun saveNote() {
        val note = currentSelectedNote() ?: return
        val content = _editableContent.value
        scope.launch {
            if (noteStore.saveNote(note, content)) {
                stopEditing(content)
            }
        }
    }

    fun deleteNote() {
        val note = currentSelectedNote() ?: return
        scope.launch {
            if (noteStore.deleteNote(note)) {
                _selectedNotePath.value = null
                stopEditing("")
            }
        }
    }

    fun selectNote(path: String) {
        val notes = noteStore.notes.value
        setSelectedNotePathInternal(path, notes, refreshContent = !_isEditing.value)
    }

    fun updateContent(content: String) {
        _editableContent.value = content
    }

    private fun currentSelectedNote(): Persisted<Note>? {
        val path = _selectedNotePath.value ?: return null
        return noteStore.notes.value.firstOrNull { it.file.canonicalPath == path }
    }

    private fun determineSelection(notes: List<Persisted<Note>>): String? {
        val current = _selectedNotePath.value
        return when {
            current != null && notes.any { it.file.canonicalPath == current } -> current
            notes.isNotEmpty() -> notes.first().file.canonicalPath
            else -> null
        }
    }

    private fun setSelectedNotePathInternal(
        path: String?,
        notes: List<Persisted<Note>>,
        refreshContent: Boolean
    ) {
        _selectedNotePath.value = path
        if (!_isEditing.value && refreshContent) {
            _editableContent.value = path?.let { requested ->
                notes.firstOrNull { it.file.canonicalPath == requested }?.value?.content
            } ?: ""
        }
    }

    private fun stopEditing(savedContent: String? = null) {
        _isEditing.value = false
        _editableContent.value = savedContent
            ?: currentSelectedNote()?.value?.content
                ?: ""
    }
}


