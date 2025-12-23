package hu.akosholloszabo.project_manager.project_manager_workshop.service

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NotePayload
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NoteRepository

interface NoteService {
    suspend fun getAll(): List<Note>
    suspend fun getById(id: Int): Note?
    suspend fun create(payload: NotePayload): Note
    suspend fun update(id: Int, payload: NotePayload): Boolean
    suspend fun delete(id: Int): Boolean
}

class NoteServiceImpl(private val repository: NoteRepository) : NoteService {
    override suspend fun getAll(): List<Note> = repository.getAll()
    override suspend fun getById(id: Int): Note? = repository.getById(id)
    override suspend fun create(payload: NotePayload): Note = repository.create(payload)
    override suspend fun update(id: Int, payload: NotePayload): Boolean = repository.update(id, payload)
    override suspend fun delete(id: Int): Boolean = repository.delete(id)
}

