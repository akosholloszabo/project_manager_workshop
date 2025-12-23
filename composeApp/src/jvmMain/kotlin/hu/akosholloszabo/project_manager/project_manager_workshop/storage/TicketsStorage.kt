package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.crypto.SecretKey

interface TicketsStorage {
    fun loadTickets(session: StorageSession?): List<Persisted<Ticket>>
    fun createTicket(
        session: StorageSession?,
        title: String? = null,
        projectId: Int? = null,
        status: TicketStatus = TicketStatus.default,
        details: String = ""
    ): Persisted<Ticket>?
    fun saveTicket(session: StorageSession?, ticket: Ticket, file: File, details: String): Boolean
    fun deleteTicket(session: StorageSession?, file: File): Boolean
}

abstract class BaseTicketsStorage : TicketsStorage {
    protected val storageSpec = FileStorageHelper.StorageSpec(
        folderName = "tickets",
        primaryExtension = ".json",
        fallbackName = "ticket",
        detailExtension = ".md"
    )

    protected val json: Json = FileStorageHelper.defaultJson

    protected inline fun <T> withTicketsDirectory(session: StorageSession?, action: (File) -> T): T? {
        val folder = session?.let { FileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
            ?: return null
        return action(folder)
    }

    protected inline fun <T> withEncryptedTicketsDirectory(session: StorageSession?, action: (StorageSession, File) -> T): T? {
        val current = session ?: return null
        val folder = File(current.folderPath, storageSpec.folderName)
            .takeIf { it.exists() || it.mkdirs() }
            ?: return null
        return action(current, folder)
    }

    protected fun readDetails(file: File): String {
        val extension = storageSpec.detailExtension ?: return ""
        return FileStorageHelper.getSidecarFile(file, extension).takeIf(File::exists)?.readText().orEmpty()
    }

    protected fun writeDetails(file: File, details: String) {
        val extension = storageSpec.detailExtension ?: return
        FileStorageHelper.writeDetails(file, storageSpec, details)
    }

    protected fun readDetailsEncrypted(file: File, key: SecretKey): String {
        val extension = storageSpec.detailExtension ?: return ""
        val detailFile = FileStorageHelper.getSidecarFile(file, extension)
        if (!detailFile.exists()) return ""
        val encrypted = detailFile.readText()
        return StorageCipher.tryDecrypt(encrypted, key).orEmpty()
    }

    protected fun writeDetailsEncrypted(file: File, key: SecretKey, details: String) {
        val extension = storageSpec.detailExtension ?: return
        val detailFile = FileStorageHelper.ensureSidecarFile(file, extension)
        detailFile.writeText(StorageCipher.encrypt(details, key))
    }

    protected inline fun <T> safe(block: () -> T): T? = runCatching(block).getOrNull()

    protected fun ticketFromFile(file: File): Persisted<Ticket>? {
        return safe {
            val content = file.readText()
            val parsed = json.decodeFromString<TicketMetadata>(content)
            val normalized = Ticket(
                id = parsed.id,
                title = parsed.title,
                projectId = parsed.projectId,
                status = TicketStatus.fromDisplay(parsed.status),
                details = readDetails(file)
            )
            Persisted(file, normalized)
        }
    }

    protected fun ticketFromFileEncrypted(file: File, key: SecretKey): Persisted<Ticket>? {
        return safe {
            val encrypted = file.readText()
            val content = StorageCipher.tryDecrypt(encrypted, key) ?: return null
            val parsed = json.decodeFromString<TicketMetadata>(content)
            val normalized = Ticket(
                id = parsed.id,
                title = parsed.title,
                projectId = parsed.projectId,
                status = TicketStatus.fromDisplay(parsed.status),
                details = readDetailsEncrypted(file, key)
            )
            Persisted(file, normalized)
        }
    }
}

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

@Serializable
private data class TicketMetadata(
    val id: Int,
    val title: String,
    val projectId: Int,
    val status: String
)
