package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import java.io.File

// TODO shouldn't the parameter be private
class PlainNotesStorage(override val fileStorageHelper: FileStorageHelper) :
    LocalNotesStorage(), NotesStorage {
    override fun loadNotes(session: StorageSession?): List<Persisted<Note>> = withNotesDirectory(session) { folder ->
        fileStorageHelper.listStorageFiles(folder, storageSpec())
            .mapNotNull(::noteFromFile)
    } ?: emptyList()

    override fun createNote(session: StorageSession?, title: String?, content: String): Persisted<Note>? {
        // TODO {} replace with =
        return withNotesDirectory(session) { folder ->
            val file = fileStorageHelper.createTimestampedFile(folder, title, storageSpec())
            val noteContent = content.ifBlank { defaultContent(title) }
            safe {
                file.writeText(noteContent)
                noteFromFile(file)
            }
        }
    }

    override fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean {
        // TODO {} replace with =
        return session?.takeIf { ensureSessionFolder(session, file) }?.let {
            safe {
                file.writeText(content)
                true
            }
        } ?: false
    }

    override fun deleteNote(session: StorageSession?, file: File): Boolean {
        // TODO {} replace with =
        return session?.takeIf { ensureSessionFolder(session, file) }?.let {
            safe { file.delete() }
        } ?: false
    }
}
