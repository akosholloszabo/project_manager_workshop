package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import java.io.File
import java.util.*

object NotesStorage {
    private val storageSpec = FileStorageHelper.StorageSpec(
        folderName = "notes",
        primaryExtension = ".md",
        fallbackName = "note"
    )


    fun ensureNotesDirectory(root: String?): File? {
        return FileStorageHelper.ensureStorageDirectory(root, storageSpec)
    }

    fun loadNotes(root: String?): List<Persisted<Note>> {
        return withNotesDirectory(root) { folder ->
            FileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull(::noteFromFile)
        } ?: emptyList()
    }

    fun createNote(root: String?, title: String? = null, content: String = ""): Persisted<Note>? {
        return withNotesDirectory(root) { folder ->
            val file = FileStorageHelper.createTimestampedFile(folder, title, storageSpec)
            val defaultTitle = title?.takeIf { it.isNotBlank() } ?: "New note"
            val defaultContent = content.ifBlank { "# $defaultTitle\n\n" }
            safe {
                file.writeText(defaultContent)
                noteFromFile(file)
            }
        }
    }

    fun saveNoteContent(file: File, content: String): Boolean {
        return safe {
            file.writeText(content)
            true
        } ?: false
    }

    fun deleteNote(file: File): Boolean {
        return safe {
            file.delete()
        } ?: false
    }

    private fun noteFromFile(file: File): Persisted<Note>? {
        return safe {
            val content = file.readText()
            val title = deriveTitle(file, content)
            val embeddedId = extractId(content)
            val normalizedId = embeddedId ?: file.canonicalPath.hashCode()
            Persisted(file, Note(normalizedId, title, content))
        }
    }

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

    private inline fun <T> withNotesDirectory(root: String?, action: (File) -> T): T? {
        val folder = ensureNotesDirectory(root) ?: return null
        return action(folder)
    }

    private inline fun <T> safe(block: () -> T): T? = runCatching(block).getOrNull()
}
