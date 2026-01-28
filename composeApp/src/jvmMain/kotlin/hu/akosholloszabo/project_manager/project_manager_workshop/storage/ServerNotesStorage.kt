package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.NotePayload
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.network.NoteServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.notes_default_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import kotlinx.coroutines.runBlocking
import java.io.File

class ServerNotesStorage(private val client: NoteServerClient) : NotesStorage {

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
        title?.takeIf { it.isNotBlank() }
            ?: getStringResource(Res.string.notes_default_title)

    override fun loadNotes(session: StorageSession?): List<Persisted<Note>> = runBlocking {
        client.getAll().map(::persistNote)
    }

    override fun createNote(session: StorageSession?, title: String?, content: String): Persisted<Note>? {
        val resolvedTitle = defaultTitle(title)
        val payload = NotePayload(
            title = resolvedTitle,
            content = content.ifBlank { "# ${defaultTitle(resolvedTitle)}\n\n" }
        )
        return runBlocking {
            persistNote(client.create(payload))
        }
    }

    override fun saveNoteContent(session: StorageSession?, file: File, content: String): Boolean =
        extractId(file)?.let { id ->
            runBlocking {
                client.update(id, NotePayload(title = deriveTitle(content, "No Title"), content = content))
                true
            }
        } ?: false

    override fun deleteNote(session: StorageSession?, file: File): Boolean =
        extractId(file)?.let { id ->
            runBlocking {
                client.delete(id)
            }
        } ?: false

    private fun persistNote(note: Note): Persisted<Note> =
        Persisted(
            file = File("server-note-${note.id}.md"),
            value = note.copy(title = deriveTitle(note.content, note.title))
        )

    private fun extractId(file: File): Int? = file.nameWithoutExtension
        .split('-')
        .lastOrNull()
        ?.toIntOrNull()
}
