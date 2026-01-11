package hu.akosholloszabo.project_manager.project_manager_workshop.service

import hu.akosholloszabo.project_manager.project_manager_workshop.entity.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.TicketRepository

class TicketService(private val repository: TicketRepository) {
    suspend fun getAll(): List<Ticket> = repository.getAll()
    suspend fun getById(id: Int): Ticket? = repository.getById(id)
    suspend fun create(payload: TicketPayload): Ticket = repository.create(payload)
    suspend fun update(id: Int, payload: TicketPayload): Boolean = repository.update(id, payload)
    suspend fun delete(id: Int): Boolean = repository.delete(id)
}

