package hu.akosholloszabo.project_manager.project_manager_workshop.repository

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import kotlinx.serialization.Serializable

interface NoteRepository {
    suspend fun getAll(): List<Note>
    suspend fun getById(id: Int): Note?
    suspend fun create(payload: NotePayload): Note
    suspend fun update(id: Int, payload: NotePayload): Boolean
    suspend fun delete(id: Int): Boolean
}

@Serializable
data class NotePayload(
    val title: String,
    val content: String
)
