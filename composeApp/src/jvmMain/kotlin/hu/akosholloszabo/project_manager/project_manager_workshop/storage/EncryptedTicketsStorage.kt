package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketMetadata
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import java.io.File

class EncryptedTicketsStorage : BaseTicketsStorage() {
    override fun loadTickets(session: StorageSession?): List<Persisted<Ticket>> {
        return withEncryptedTicketsDirectory(session) { current, folder ->
            val key = current.encryptionKey ?: return@withEncryptedTicketsDirectory emptyList()
            FileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull { ticketFromFileEncrypted(it, key) }
        } ?: emptyList()
    }

    override fun createTicket(
        session: StorageSession?,
        title: String?,
        projectId: Int?,
        status: TicketStatus,
        details: String
    ): Persisted<Ticket>? {
        return withEncryptedTicketsDirectory(session) { current, folder ->
            val key = current.encryptionKey ?: return@withEncryptedTicketsDirectory null
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
                ticketFromFileEncrypted(file, key)
            }
        }
    }

    override fun saveTicket(session: StorageSession?, ticket: Ticket, file: File, details: String): Boolean {
        val key = session?.encryptionKey ?: return false
        return safe {
            val metadata = TicketMetadata(
                ticket.id,
                ticket.title,
                ticket.projectId,
                ticket.status.displayText
            )
            file.writeText(StorageCipher.encrypt(json.encodeToString(metadata), key))
            writeDetailsEncrypted(file, key, details)
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

