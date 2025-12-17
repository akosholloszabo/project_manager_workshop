package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object NotesStorage {
    private const val NOTES_FOLDER_NAME = "notes"
    private const val NOTE_EXTENSION = ".md"
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)

    data class PersistedNote(val file: File, val note: Note)

    fun ensureNotesDirectory(root: String?): File? {
        val folder = root?.let { File(it, NOTES_FOLDER_NAME) } ?: return null
        if (!folder.exists()) {
            if (!folder.mkdirs()) return null
        }
        return folder
    }

    fun loadNotes(root: String?): List<PersistedNote> {
        val folder = ensureNotesDirectory(root) ?: return emptyList()
        return folder.listFiles { file ->
            file.isFile && file.extension.equals("md", ignoreCase = true)
        }?.mapNotNull(::noteFromFile)?.sortedByDescending { it.file.lastModified() } ?: emptyList()
    }

    fun createNote(root: String?, title: String? = null, content: String = ""): PersistedNote? {
        val folder = ensureNotesDirectory(root) ?: return null
        val baseName = sanitizeFileName(title ?: "note")
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val file = File(folder, "$baseName-$timestamp$NOTE_EXTENSION")
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

    private fun sanitizeFileName(raw: String): String {
        val sanitized = raw.trim().ifEmpty { "note" }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .replace(Regex("\\s+"), "-")
        return sanitized.trim('-').ifEmpty { "note" }
    }
}

