package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object TicketsStorage {
    private const val TICKETS_FOLDER_NAME = "tickets"
    private const val TICKET_EXTENSION = ".json"
    private const val DETAILS_EXTENSION = ".md"
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)
    private val json = Json { encodeDefaults = true; prettyPrint = true }

    data class PersistedTicket(val file: File, val ticket: Ticket)

    @Serializable
    private data class TicketMetadata(
        val id: Int,
        val title: String,
        val projectId: Int,
        val status: String
    )

    fun ensureTicketsDirectory(root: String?): File? {
        val folder = root?.let { File(it, TICKETS_FOLDER_NAME) } ?: return null
        if (!folder.exists() && !folder.mkdirs()) return null
        return folder
    }

    fun loadTickets(root: String?): List<PersistedTicket> {
        val folder = ensureTicketsDirectory(root) ?: return emptyList()
        return folder.listFiles { file ->
            file.isFile && file.extension.equals(TICKET_EXTENSION.trimStart('.'), ignoreCase = true)
        }?.mapNotNull(::ticketFromFile)?.sortedByDescending { it.file.lastModified() } ?: emptyList()
    }

    fun createTicket(
        root: String?,
        title: String? = null,
        projectId: Int? = null,
        status: TicketStatus = TicketStatus.default,
        details: String = ""
    ): PersistedTicket? {
        val folder = ensureTicketsDirectory(root) ?: return null
        val baseName = sanitizeFileName(title ?: "ticket")
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val file = File(folder, "$baseName-$timestamp$TICKET_EXTENSION")
        val defaultTitle = title?.takeIf { it.isNotBlank() } ?: "New ticket"
        val ticket = Ticket(
            id = 0,
            title = defaultTitle,
            projectId = projectId ?: 0,
            status = status,
            details = details
        )
        return runCatching {
            saveTicket(ticket, file, details)
            ticketFromFile(file)
        }.getOrNull()
    }

    fun saveTicket(ticket: Ticket, file: File, details: String): Boolean {
        return runCatching {
            val metadata = TicketMetadata(ticket.id, ticket.title, ticket.projectId, ticket.status.displayText)
            file.writeText(json.encodeToString(metadata))
            saveDetails(file, details)
            true
        }.getOrDefault(false)
    }

    fun deleteTicket(file: File): Boolean {
        return runCatching {
            getDetailsFile(file).delete()
            file.delete()
        }.getOrDefault(false)
    }

    private fun ticketFromFile(file: File): PersistedTicket? {
        return runCatching {
            val content = file.readText()
            val parsed = json.decodeFromString<TicketMetadata>(content)
            val normalized = Ticket(
                id = file.canonicalPath.hashCode(),
                title = parsed.title,
                projectId = parsed.projectId,
                status = TicketStatus.fromDisplay(parsed.status),
                details = loadDetails(file)
            )
            PersistedTicket(file, normalized)
        }.getOrNull()
    }

    private fun getDetailsFile(ticketFile: File): File {
        val parent = ticketFile.parentFile ?: ticketFile
        return File(parent, ticketFile.nameWithoutExtension + DETAILS_EXTENSION)
    }

    private fun ensureDetailsFile(ticketFile: File): File {
        val detailsFile = getDetailsFile(ticketFile)
        if (!detailsFile.exists()) {
            detailsFile.writeText("")
        }
        return detailsFile
    }

    private fun loadDetails(ticketFile: File): String {
        return runCatching {
            ensureDetailsFile(ticketFile).readText()
        }.getOrDefault("")
    }

    private fun saveDetails(ticketFile: File, details: String) {
        val detailsFile = ensureDetailsFile(ticketFile)
        detailsFile.writeText(details)
    }

    private fun sanitizeFileName(raw: String): String {
        val sanitized = raw.trim().ifEmpty { "ticket" }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .replace(Regex("\\s+"), "-")
        return sanitized.trim('-').ifEmpty { "ticket" }
    }
}
