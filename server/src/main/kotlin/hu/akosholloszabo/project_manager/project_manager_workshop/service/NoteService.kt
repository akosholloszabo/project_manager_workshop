package hu.akosholloszabo.project_manager.project_manager_workshop.service

import hu.akosholloszabo.project_manager.project_manager_workshop.entity.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.NotePayload
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NoteRepository

class NoteService(private val repository: NoteRepository) {
    suspend fun getAll(): List<Note> = repository.getAll()
    suspend fun getById(id: Int): Note? = repository.getById(id)
    suspend fun create(payload: NotePayload): Note = repository.create(payload)
    suspend fun update(id: Int, payload: NotePayload): Boolean = repository.update(id, payload)
    suspend fun delete(id: Int): Boolean = repository.delete(id)
}

