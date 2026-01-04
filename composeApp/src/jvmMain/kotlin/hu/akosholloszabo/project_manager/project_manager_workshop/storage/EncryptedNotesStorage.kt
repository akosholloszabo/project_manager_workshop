package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import java.io.File

class EncryptedNotesStorage : BaseNotesStorage(), NotesStorage {
    override fun loadNotes(session: StorageSession?): List<Persisted<Note>> = withNotesDirectory(session) { folder ->
        val key = session?.encryptionKey ?: return@withNotesDirectory emptyList()
        FileStorageHelper.listStorageFiles(folder, storageSpec())
            .mapNotNull { noteFromFileEncrypted(it, key) }
    } ?: emptyList()

    override fun createNote(session: StorageSession?, title: String?, content: String): Persisted<Note>? {
        val key = session?.encryptionKey ?: return null
        return withNotesDirectory(session) { folder ->
            val file = FileStorageHelper.createTimestampedFile(folder, title, storageSpec())
            val noteContent = content.ifBlank { defaultContent(title) }
            safe {
                file.writeText(StorageCipher.encrypt(noteContent, key))
                noteFromFileEncrypted(file, key)
            }
        }
    }

    override fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean {
        val key = session?.encryptionKey ?: return false
        return session.takeIf { ensureSessionFolder(it, file) }?.let {
            safe {
                file.writeText(StorageCipher.encrypt(content, key))
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

