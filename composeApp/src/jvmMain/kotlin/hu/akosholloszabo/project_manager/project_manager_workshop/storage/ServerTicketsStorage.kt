package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.network.TicketServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_default_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
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
        val resolvedTitle = title.takeIf { it.isNotBlank() }
            ?: getStringResource(Res.string.ticket_default_title)
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

    override fun saveTicket(session: StorageSession?, ticket: Ticket, file: File, details: String): Boolean =
        runBlocking {
            val payload = TicketPayload(
                title = ticket.title,
                projectId = ticket.projectId,
                status = ticket.status,
                details = details
            )
            client.update(ticket.id, payload)
            true
        }

    override fun deleteTicket(session: StorageSession?, file: File): Boolean =
        file.nameWithoutExtension
            .split('-')
            .lastOrNull()
            ?.toIntOrNull()?.let { id ->
                runBlocking {
                    client.delete(id)
                }
            } ?: false

    private fun persistTicket(ticket: Ticket): Persisted<Ticket> =
        Persisted(File("server-ticket-${ticket.id}.json"), ticket)

}
