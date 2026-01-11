package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import java.io.File
import javax.crypto.SecretKey

class EncryptedNotesStorage(
    val storageCipher: StorageCipher,
    override val fileStorageHelper: FileStorageHelper
) : LocalNotesStorage(), NotesStorage {
    override fun loadNotes(session: StorageSession?): List<Persisted<Note>> = withNotesDirectory(session) { folder ->
        val key = session?.encryptionKey ?: return@withNotesDirectory emptyList()
        fileStorageHelper.listStorageFiles(folder, storageSpec())
            .mapNotNull { noteFromFileEncrypted(it, key) }
    } ?: emptyList()

    override fun createNote(session: StorageSession?, title: String?, content: String): Persisted<Note>? {
        val key = session?.encryptionKey ?: return null
        return withNotesDirectory(session) { folder ->
            val file = fileStorageHelper.createTimestampedFile(folder, title, storageSpec())
            val noteContent = content.ifBlank { defaultContent(title) }
            safe {
                file.writeText(storageCipher.encrypt(noteContent, key))
                noteFromFileEncrypted(file, key)
            }
        }
    }

    override fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean {
        val key = session?.encryptionKey ?: return false
        return session.takeIf { ensureSessionFolder(it, file) }?.let {
            safe {
                file.writeText(storageCipher.encrypt(content, key))
                true
            }
        } ?: false
    }

    override fun deleteNote(session: StorageSession?, file: File): Boolean {
        return session?.takeIf { ensureSessionFolder(session, file) }?.let {
            safe { file.delete() }
        } ?: false
    }


    private fun noteFromFileEncrypted(file: File, key: SecretKey): Persisted<Note>? {
        return safe {
            val encryptedContent = file.readText()
            val content = storageCipher.tryDecrypt(encryptedContent, key) ?: return null
            val parsedTitle = deriveTitle(file, content)
            val embeddedId = extractId(content)
            val normalizedId = embeddedId ?: file.canonicalPath.hashCode()
            Persisted(file, Note(normalizedId, parsedTitle, content))
        }
    }
}

