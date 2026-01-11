package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import org.koin.core.component.KoinComponent
import java.io.File

interface NotesStorage : KoinComponent {
    fun loadNotes(session: StorageSession?): List<Persisted<Note>>
    fun createNote(session: StorageSession?, title: String? = null, content: String = ""): Persisted<Note>?
    fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean
    fun deleteNote(session: StorageSession?, file: File): Boolean
}
