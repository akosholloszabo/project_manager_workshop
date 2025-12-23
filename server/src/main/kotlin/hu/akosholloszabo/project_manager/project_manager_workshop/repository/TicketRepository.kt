package hu.akosholloszabo.project_manager.project_manager_workshop.repository

import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import kotlinx.serialization.Serializable

interface TicketRepository {
    suspend fun getAll(): List<Ticket>
    suspend fun getById(id: Int): Ticket?
    suspend fun create(payload: TicketPayload): Ticket
    suspend fun update(id: Int, payload: TicketPayload): Boolean
    suspend fun delete(id: Int): Boolean
}

@Serializable
data class TicketPayload(
    val title: String,
    val projectId: Int,
    val status: TicketStatus = TicketStatus.default,
    val details: String = ""
)
