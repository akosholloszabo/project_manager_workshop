package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSpec
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.notes_default_title
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.notes_default_untitled
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import java.io.File
import java.util.*

abstract class LocalNotesStorage : NotesStorage {
    abstract val fileStorageHelper: FileStorageHelper

    protected val storageSpec = StorageSpec(
        folderName = "notes",
        primaryExtension = ".md",
        fallbackName = "note"
    )

    protected inline fun <T> withNotesDirectory(session: StorageSession?, action: (File) -> T): T? {
        val folder = session?.let { fileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
            ?: return null
        return action(folder)
    }

    protected fun ensureSessionFolder(session: StorageSession, file: File): Boolean {
        val canonicalRoot = File(session.folderPath).canonicalPath
        return file.canonicalPath.startsWith(canonicalRoot)
    }

    protected fun defaultTitle(title: String?): String =
        title?.takeIf { it.isNotBlank() } ?: getStringResource(Res.string.notes_default_title)

    protected fun defaultContent(title: String?): String = "# ${defaultTitle(title)}\n\n"

    protected inline fun <T> safe(block: () -> T): T? = runCatching(block).getOrNull()

    protected fun noteFromFile(file: File): Persisted<Note>? =
        safe {
            val content = file.readText()
            val parsedTitle = deriveTitle(file, content)
            val embeddedId = extractId(content)
            val normalizedId = embeddedId ?: file.canonicalPath.hashCode()
            Persisted(file, Note(normalizedId, parsedTitle, content))
        }

    protected fun deriveTitle(file: File, content: String): String {
        val heading = content.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("#") }
            ?.removePrefix("#")
            ?.trim()
        return heading?.takeIf { it.isNotBlank() } ?: fallbackTitle(file)
    }

    private fun fallbackTitle(file: File): String {
        val fallback = file.nameWithoutExtension
            .replace(Regex("[-_]"), " ")
            .trim()
            .takeUnless { it.isBlank() }
        return fallback?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
            ?: getStringResource(Res.string.notes_default_untitled)
    }

    protected fun extractId(content: String): Int? {
        val idLine = content.lineSequence()
            .firstOrNull { it.startsWith("<!-- id:") && it.endsWith("-->") }
            ?: return null
        return idLine.removePrefix("<!-- id:").removeSuffix("-->").trim().toIntOrNull()
    }
}
