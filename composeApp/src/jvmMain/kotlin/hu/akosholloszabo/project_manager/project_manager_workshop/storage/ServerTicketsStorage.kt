package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.network.TicketServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText
import kotlinx.coroutines.runBlocking
import java.io.File

class ServerTicketsStorage(private val client: TicketServerClient) : TicketsStorage {
    override fun loadTickets(session: StorageSession?): List<Persisted<Ticket>> = runBlocking {
        client.getAll().map(::persistTicket)
    }

    override fun createTicket(
        session: StorageSession?,
        title: String,
        projectId: Int,
        status: TicketStatus,
        details: String
    ): Persisted<Ticket> {
        val resolvedTitle = title.takeIf { it.isNotBlank() } ?: getText("ticket.default.title")
        val payload = TicketPayload(
            title = resolvedTitle,
            projectId = projectId,
            status = status,
            details = details
        )
        return runBlocking {
            persistTicket(client.create(payload))
        }
    }

    override fun saveTicket(session: StorageSession?, ticket: Ticket, file: File, details: String): Boolean {
        return runBlocking {
            val payload = TicketPayload(
                title = ticket.title,
                projectId = ticket.projectId,
                status = ticket.status,
                details = details
            )
            client.update(ticket.id, payload)
        }
    }

    override fun deleteTicket(session: StorageSession?, file: File): Boolean {
        val id = extractId(file) ?: return false
        return runBlocking {
            client.delete(id)
        }
    }

    private fun persistTicket(ticket: Ticket): Persisted<Ticket> = Persisted(ticketFile(ticket.id), ticket)

    private fun ticketFile(id: Int): File = File("server-ticket-$id.json")

    private fun extractId(file: File): Int? = file.nameWithoutExtension
        .split('-')
        .lastOrNull()
        ?.toIntOrNull()
}
