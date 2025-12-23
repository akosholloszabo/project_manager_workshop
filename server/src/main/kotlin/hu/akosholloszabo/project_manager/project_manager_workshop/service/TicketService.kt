package hu.akosholloszabo.project_manager.project_manager_workshop.service

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.TicketPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.TicketRepository

interface TicketService {
    suspend fun getAll(): List<Ticket>
    suspend fun getById(id: Int): Ticket?
    suspend fun create(payload: TicketPayload): Ticket
    suspend fun update(id: Int, payload: TicketPayload): Boolean
    suspend fun delete(id: Int): Boolean
}

class TicketServiceImpl(private val repository: TicketRepository) : TicketService {
    override suspend fun getAll(): List<Ticket> = repository.getAll()
    override suspend fun getById(id: Int): Ticket? = repository.getById(id)
    override suspend fun create(payload: TicketPayload): Ticket = repository.create(payload)
    override suspend fun update(id: Int, payload: TicketPayload): Boolean = repository.update(id, payload)
    override suspend fun delete(id: Int): Boolean = repository.delete(id)
}

