package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSpec
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Strings
import java.io.File
import java.util.*

abstract class LocalNotesStorage(private val strings: Strings) : NotesStorage {
    abstract val fileStorageHelper: FileStorageHelper

    protected val storageSpec = StorageSpec(
        folderName = "notes",
        primaryExtension = ".md",
        fallbackName = "note"
    )

    private val newNoteText by lazy { strings.require("notes.default.title") }
    private val untitledText by lazy { strings.require("notes.default.untitled") }

    protected inline fun <T> withNotesDirectory(session: StorageSession?, action: (File) -> T): T? {
        val folder = session?.let { fileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
            ?: return null
        return action(folder)
    }

    protected fun ensureSessionFolder(session: StorageSession, file: File): Boolean {
        val canonicalRoot = File(session.folderPath).canonicalPath
        return file.canonicalPath.startsWith(canonicalRoot)
    }

    protected fun defaultTitle(title: String?): String = title?.takeIf { it.isNotBlank() } ?: newNoteText

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

    protected fun storageSpec() = storageSpec

    protected fun deriveTitle(content: String, fallback: String): String {
        val firstLineTitle = content.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.removePrefix("#")
            ?.trim()
            ?.takeUnless { it.isBlank() }
        val resolvedFallback = fallback.takeUnless { it.isBlank() } ?: untitledText
        return firstLineTitle ?: resolvedFallback
    }

    protected fun deriveTitle(file: File, content: String): String {
        return deriveTitle(content, fallbackTitle(file))
    }

    private fun fallbackTitle(file: File): String {
        val fallback = file.nameWithoutExtension
            .replace(Regex("[-_]"), " ")
            .trim()
            .takeUnless { it.isBlank() }
        return fallback?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            ?: untitledText
    }

    protected fun extractId(content: String): Int? {
        val idLine = content.lineSequence()
            .firstOrNull { it.startsWith("<!-- id:") && it.endsWith("-->") }
            ?: return null
        return idLine.removePrefix("<!-- id:").removeSuffix("-->").trim().toIntOrNull()
    }
}
