package hu.akosholloszabo.project_manager.project_manager_workshop.model

data class NotesScreenState(
    val notes: List<Persisted<Note>> = emptyList(),
    val selectedNotePath: String? = null,
    val selectedNote: Persisted<Note>? = null,
    val isEditing: Boolean = false,
    val editableContent: String = ""
)
