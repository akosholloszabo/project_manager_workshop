package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import java.io.File

class PlainNotesStorage : BaseNotesStorage(), NotesStorage {
    override fun loadNotes(session: StorageSession?): List<Persisted<Note>> = withNotesDirectory(session) { folder ->
        FileStorageHelper.listStorageFiles(folder, storageSpec())
            .mapNotNull(::noteFromFile)
    } ?: emptyList()

    override fun createNote(session: StorageSession?, title: String?, content: String): Persisted<Note>? {
        return withNotesDirectory(session) { folder ->
            val file = FileStorageHelper.createTimestampedFile(folder, title, storageSpec())
            val noteContent = content.ifBlank { defaultContent(title) }
            safe {
                file.writeText(noteContent)
                noteFromFile(file)
            }
        }
    }

    override fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean {
        return session?.takeIf { ensureSessionFolder(session, file) }?.let {
            safe {
                file.writeText(content)
                true
            }
        } ?: false
    }

    override fun deleteNote(session: StorageSession?, file: File): Boolean {
        return session?.takeIf { ensureSessionFolder(session, file) }?.let {
            safe { file.delete() }
        } ?: false
    }
}

