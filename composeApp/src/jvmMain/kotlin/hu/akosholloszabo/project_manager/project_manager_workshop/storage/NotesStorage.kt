package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import java.io.File
import java.util.*

object NotesStorage {
    private val storageSpec = FileStorageHelper.StorageSpec(
        folderName = "notes",
        primaryExtension = ".md",
        fallbackName = "note"
    )

    data class PersistedNote(val file: File, val note: Note)

    fun ensureNotesDirectory(root: String?): File? {
        return FileStorageHelper.ensureStorageDirectory(root, storageSpec)
    }

    fun loadNotes(root: String?): List<PersistedNote> {
        val folder = ensureNotesDirectory(root) ?: return emptyList()
        return FileStorageHelper.listStorageFiles(folder, storageSpec)
            .mapNotNull(::noteFromFile)
    }

    fun createNote(root: String?, title: String? = null, content: String = ""): PersistedNote? {
        val folder = ensureNotesDirectory(root) ?: return null
        val file = FileStorageHelper.createTimestampedFile(folder, title, storageSpec)
        val defaultTitle = title?.takeIf { it.isNotBlank() } ?: "New note"
        val defaultContent = content.ifBlank { "# $defaultTitle\n\n" }
        return runCatching {
            file.writeText(defaultContent)
            noteFromFile(file)
        }.getOrNull()
    }

    fun saveNoteContent(file: File, content: String): Boolean {
        return runCatching {
            file.writeText(content)
            true
        }.getOrDefault(false)
    }

    fun deleteNote(file: File): Boolean {
        return runCatching {
            file.delete()
        }.getOrDefault(false)
    }

    private fun noteFromFile(file: File): PersistedNote? {
        return runCatching {
            val content = file.readText()
            val title = deriveTitle(file, content)
            val id = file.canonicalPath.hashCode()
            PersistedNote(file, Note(id, title, content))
        }.getOrNull()
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
}
