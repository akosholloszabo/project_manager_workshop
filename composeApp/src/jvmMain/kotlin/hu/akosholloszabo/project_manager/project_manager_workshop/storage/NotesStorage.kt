package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import java.io.File
import java.util.*
import javax.crypto.SecretKey

interface NotesStorage {
    fun loadNotes(session: StorageSession?): List<Persisted<Note>>
    fun createNote(session: StorageSession?, title: String? = null, content: String = ""): Persisted<Note>?
    fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean
    fun deleteNote(session: StorageSession?, file: File): Boolean
}

abstract class BaseNotesStorage {
    protected val storageSpec = FileStorageHelper.StorageSpec(
        folderName = "notes",
        primaryExtension = ".md",
        fallbackName = "note"
    )

    protected inline fun <T> withNotesDirectory(session: StorageSession?, action: (File) -> T): T? {
        val folder = session?.let { FileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
            ?: return null
        return action(folder)
    }

    protected fun ensureSessionFolder(session: StorageSession, file: File): Boolean {
        val canonicalRoot = File(session.folderPath).canonicalPath
        return file.canonicalPath.startsWith(canonicalRoot)
    }

    protected fun defaultTitle(title: String?): String = title?.takeIf { it.isNotBlank() } ?: "New note"

    protected fun defaultContent(title: String?): String = "# ${defaultTitle(title)}\n\n"

    protected inline fun <T> safe(block: () -> T): T? = runCatching(block).getOrNull()

    protected fun noteFromFile(file: File): Persisted<Note>? {
        return safe {
            val content = file.readText()
            val parsedTitle = deriveTitle(file, content)
            val embeddedId = extractId(content)
            val normalizedId = embeddedId ?: file.canonicalPath.hashCode()
            Persisted(file, Note(normalizedId, parsedTitle, content))
        }
    }

    protected fun noteFromFileEncrypted(file: File, key: SecretKey): Persisted<Note>? {
        return safe {
            val encryptedContent = file.readText()
            val content = StorageCipher.tryDecrypt(encryptedContent, key) ?: return null
            val parsedTitle = deriveTitle(file, content)
            val embeddedId = extractId(content)
            val normalizedId = embeddedId ?: file.canonicalPath.hashCode()
            Persisted(file, Note(normalizedId, parsedTitle, content))
        }
    }

    protected fun storageSpec() = storageSpec

    private fun deriveTitle(file: File, content: String): String {
        val firstLineTitle = content.lineSequence().firstOrNull()?.trim()?.let { line ->
            val cleanLine = line.removePrefix("#").trim()
            cleanLine.takeUnless { it.isBlank() }
        }
        if (!firstLineTitle.isNullOrBlank()) {
            return firstLineTitle
        }

        val fallback = file.nameWithoutExtension
            .replace(Regex("[-_]"), " ")
            .trim()
            .takeUnless { it.isBlank() }

        return fallback?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            ?: "Untitled"
    }

    private fun extractId(content: String): Int? {
        val idLine = content.lineSequence()
            .firstOrNull { it.startsWith("<!-- id:") && it.endsWith("-->") }
            ?: return null
        return idLine.removePrefix("<!-- id:").removeSuffix("-->").trim().toIntOrNull()
    }
}

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
        return session?.takeIf { ensureSessionFolder(it, file) }?.let {
            safe { file.delete() }
        } ?: false
    }
}
