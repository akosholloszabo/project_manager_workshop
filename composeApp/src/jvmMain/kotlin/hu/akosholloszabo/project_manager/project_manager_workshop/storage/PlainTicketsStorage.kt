package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketMetadata
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_default_title
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import java.io.File
import java.util.UUID.randomUUID

class PlainTicketsStorage(
    fileStorageHelper: FileStorageHelper
) : LocalTicketsStorage(fileStorageHelper) {
    private val newTicketText = getStringResource(Res.string.ticket_default_title)
    override fun loadTickets(session: StorageSession?): List<Persisted<Ticket>> =
        withTicketsDirectory(session) { folder ->
            fileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull(::ticketFromFile)
        } ?: emptyList()

    override fun createTicket(
        session: StorageSession?,
        title: String,
        projectId: Int,
        status: TicketStatus,
        details: String
    ): Persisted<Ticket>? =
        withTicketsDirectory(session) { folder ->
            val file = fileStorageHelper.createTimestampedFile(folder, title, storageSpec)
            val defaultTitle = title.takeIf { it.isNotBlank() } ?: newTicketText
            val ticket = Ticket(
                id = randomUUID().hashCode(),
                title = defaultTitle,
                projectId = projectId,
                status = status,
                details = details
            )
            safe {
                saveTicket(session, ticket, file, details)
                ticketFromFile(file)
            }
        }

    override fun saveTicket(session: StorageSession?, ticket: Ticket, file: File, details: String): Boolean =
        session?.let {
            safe {
                val metadata = TicketMetadata(
                    id = ticket.id,
                    title = ticket.title,
                    projectId = ticket.projectId,
                    status = ticket.status?.name
                )
                file.writeText(json.encodeToString(metadata))
                writeDetails(file, details)
                true
            }
        } ?: false

    override fun deleteTicket(session: StorageSession?, file: File): Boolean =
        session?.let {
            safe {
                fileStorageHelper.deleteDetails(file, storageSpec)
                file.delete()
            }
        } ?: false
}
