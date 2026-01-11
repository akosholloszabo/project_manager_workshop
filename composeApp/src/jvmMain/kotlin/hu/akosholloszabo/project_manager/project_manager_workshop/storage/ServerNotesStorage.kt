package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.NotePayload
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.network.NoteServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Strings
import kotlinx.coroutines.runBlocking
import java.io.File

class ServerNotesStorage(private val client: NoteServerClient, private val strings: Strings) : NotesStorage {

    private fun deriveTitle(content: String, fallback: String): String {
        val firstLineTitle = content.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.removePrefix("#")
            ?.trim()
            ?.takeUnless { it.isBlank() }
        val resolvedFallback = fallback.takeUnless { it.isBlank() } ?: "Untitled"
        return firstLineTitle ?: resolvedFallback
    }

    private fun defaultTitle(title: String?): String =
        title?.takeIf { it.isNotBlank() } ?: strings.require("notes.default.title")

    private fun defaultContent(title: String?): String = "# ${defaultTitle(title)}\n\n"

    override fun loadNotes(session: StorageSession?): List<Persisted<Note>> = runBlocking {
        client.getAll().map(::persistNote)
    }

    override fun createNote(session: StorageSession?, title: String?, content: String): Persisted<Note>? {
        val resolvedTitle = defaultTitle(title)
        val payload = NotePayload(
            title = resolvedTitle,
            content = content.ifBlank { defaultContent(resolvedTitle) }
        )
        return runBlocking {
            persistNote(client.create(payload))
        }
    }

    override fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean {
        val id = extractId(file) ?: return false
        return runBlocking {
            client.update(id, NotePayload(title = deriveTitle(content, "No Title"), content = content))
        }
    }

    override fun deleteNote(session: StorageSession?, file: File): Boolean {
        val id = extractId(file) ?: return false
        return runBlocking {
            client.delete(id)
        }
    }

    private fun persistNote(note: Note): Persisted<Note> {
        val normalizedTitle = deriveTitle(note.content, note.title)
        return Persisted(noteFile(note.id), note.copy(title = normalizedTitle))
    }

    private fun noteFile(id: Int): File = File("server-note-$id.md")

    private fun extractId(file: File): Int? = file.nameWithoutExtension
        .split('-')
        .lastOrNull()
        ?.toIntOrNull()
}
