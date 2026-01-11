package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketMetadata
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getTextOrException
import java.io.File
import java.util.UUID.randomUUID
import javax.crypto.SecretKey

class EncryptedTicketsStorage(
    val storageCipher: StorageCipher,
    fileStorageHelper: FileStorageHelper
) : LocalTicketsStorage(fileStorageHelper) {
    override fun loadTickets(session: StorageSession?): List<Persisted<Ticket>> {
        return withEncryptedTicketsDirectory(session) { current, folder ->
            val key = current.encryptionKey ?: return@withEncryptedTicketsDirectory emptyList()
            fileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull { ticketFromFileEncrypted(it, key) }
        } ?: emptyList()
    }

    override fun createTicket(
        session: StorageSession?,
        title: String,
        projectId: Int,
        status: TicketStatus,
        details: String
    ): Persisted<Ticket>? {
        return withEncryptedTicketsDirectory(session) { current, folder ->
            val key = current.encryptionKey ?: return@withEncryptedTicketsDirectory null
            val file = fileStorageHelper.createTimestampedFile(folder, title, storageSpec)
            val defaultTitle = title?.takeIf { it.isNotBlank() } ?: getKoin().getTextOrException("ticket.new")
            val ticket = Ticket(
                id = randomUUID().hashCode(),
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
                ticket.status?.name
            )
            file.writeText(storageCipher.encrypt(json.encodeToString(metadata), key))
            writeDetailsEncrypted(file, key, details)
            true
        } ?: false
    }

    override fun deleteTicket(session: StorageSession?, file: File): Boolean {
        return session?.let {
            safe {
                fileStorageHelper.deleteDetails(file, storageSpec)
                file.delete()
            }
        } ?: false
    }

    private fun ticketFromFileEncrypted(file: File, key: SecretKey): Persisted<Ticket>? {
        return safe {
            val encrypted = file.readText()
            val content = storageCipher.tryDecrypt(encrypted, key) ?: return null
            val parsed = json.decodeFromString<TicketMetadata>(content)
            val normalized = Ticket(
                id = parsed.id,
                title = parsed.title,
                projectId = parsed.projectId,
                status = parsed.status?.let { TicketStatus.fromName(it) },
                details = readDetailsEncrypted(file, key)
            )
            Persisted(file, normalized)
        }
    }

    private fun readDetailsEncrypted(file: File, key: SecretKey): String {
        val extension = storageSpec.detailExtension ?: return ""
        val detailFile = fileStorageHelper.getSidecarFile(file, extension)
        if (!detailFile.exists()) return ""
        val encrypted = detailFile.readText()
        return storageCipher.tryDecrypt(encrypted, key).orEmpty()
    }

    private fun writeDetailsEncrypted(file: File, key: SecretKey, details: String) {
        val extension = storageSpec.detailExtension ?: return
        val detailFile = fileStorageHelper.ensureSidecarFile(file, extension)
        detailFile.writeText(storageCipher.encrypt(details, key))
    }
}
