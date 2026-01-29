package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import java.io.File

interface TicketsStorage {
    fun loadTickets(session: StorageSession?): List<Persisted<Ticket>>
    fun createTicket(
        session: StorageSession?,
        title: String,
        projectId: Int,
        status: TicketStatus,
        details: String
    ): Persisted<Ticket>?

    fun saveTicket(session: StorageSession?, ticket: Ticket, file: File, details: String): Boolean
    fun deleteTicket(session: StorageSession?, file: File): Boolean
}
