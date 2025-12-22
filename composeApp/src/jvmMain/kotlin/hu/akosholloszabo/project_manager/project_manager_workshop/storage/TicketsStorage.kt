package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File

object TicketsStorage {
    private val storageSpec = FileStorageHelper.StorageSpec(
        folderName = "tickets",
        primaryExtension = ".json",
        fallbackName = "ticket",
        detailExtension = ".md"
    )

    private val json = FileStorageHelper.defaultJson

    @Serializable
    private data class TicketMetadata(
        val id: Int,
        val title: String,
        val projectId: Int,
        val status: String
    )

    fun ensureTicketsDirectory(root: String?): File? {
        return FileStorageHelper.ensureStorageDirectory(root, storageSpec)
    }

    fun loadTickets(root: String?): List<Persisted<Ticket>> = withTicketsDirectory(root) { folder ->
        FileStorageHelper.listStorageFiles(folder, storageSpec)
            .mapNotNull(::ticketFromFile)
    } ?: emptyList()

    fun createTicket(
        root: String?,
        title: String? = null,
        projectId: Int? = null,
        status: TicketStatus = TicketStatus.default,
        details: String = ""
    ): Persisted<Ticket>? {
        return withTicketsDirectory(root) { folder ->
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
                saveTicket(ticket, file, details)
                ticketFromFile(file)
            }
        }
    }

    fun saveTicket(ticket: Ticket, file: File, details: String): Boolean {
        return safe {
            val metadata = TicketMetadata(
                ticket.id,
                ticket.title,
                ticket.projectId,
                ticket.status.displayText
            )
            file.writeText(json.encodeToString(metadata))
            FileStorageHelper.writeDetails(file, storageSpec, details)
            true
        } ?: false
    }

    fun deleteTicket(file: File): Boolean {
        return safe {
            FileStorageHelper.deleteDetails(file, storageSpec)
            file.delete()
        } ?: false
    }

    private fun ticketFromFile(file: File): Persisted<Ticket>? {
        return safe {
            val content = file.readText()
            val parsed = json.decodeFromString<TicketMetadata>(content)
            val normalized = Ticket(
                id = parsed.id,
                title = parsed.title,
                projectId = parsed.projectId,
                status = TicketStatus.fromDisplay(parsed.status),
                details = FileStorageHelper.readDetails(file, storageSpec)
            )
            Persisted(file, normalized)
        }
    }

    private inline fun <T> withTicketsDirectory(root: String?, action: (File) -> T): T? {
        val folder = ensureTicketsDirectory(root) ?: return null
        return action(folder)
    }

    private inline fun <T> safe(block: () -> T): T? = runCatching(block).getOrNull()
}
