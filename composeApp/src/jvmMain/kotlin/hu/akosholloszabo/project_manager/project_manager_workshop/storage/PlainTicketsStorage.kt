package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketMetadata
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import java.io.File

class PlainTicketsStorage : BaseTicketsStorage() {
    override fun loadTickets(session: StorageSession?): List<Persisted<Ticket>> {
        return withTicketsDirectory(session) { folder ->
            FileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull(::ticketFromFile)
        } ?: emptyList()
    }

    override fun createTicket(
        session: StorageSession?,
        title: String?,
        projectId: Int?,
        status: TicketStatus,
        details: String
    ): Persisted<Ticket>? {
        return withTicketsDirectory(session) { folder ->
            val file = FileStorageHelper.createTimestampedFile(folder, title, storageSpec)
            val defaultTitle = title?.takeIf { it.isNotBlank() } ?: "New ticket"
            val ticket = Ticket(
                id = EntityIdGenerator.newId(),
                title = defaultTitle,
                projectId = projectId ?: 0,
                status = status,
                details = details
            )
            safe {
                saveTicket(session, ticket, file, details)
                ticketFromFile(file)
            }
        }
    }

    override fun saveTicket(session: StorageSession?, ticket: Ticket, file: File, details: String): Boolean {
        if (session == null) return false
        return safe {
            val metadata = TicketMetadata(
                ticket.id,
                ticket.title,
                ticket.projectId,
                ticket.status.displayText
            )
            file.writeText(json.encodeToString(metadata))
            writeDetails(file, details)
            true
        } ?: false
    }

    override fun deleteTicket(session: StorageSession?, file: File): Boolean {
        return session?.let {
            safe {
                FileStorageHelper.deleteDetails(file, storageSpec)
                file.delete()
            }
        } ?: false
    }
}
