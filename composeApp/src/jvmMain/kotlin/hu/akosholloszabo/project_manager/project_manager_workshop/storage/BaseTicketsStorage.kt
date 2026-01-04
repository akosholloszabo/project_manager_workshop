package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketMetadata
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import kotlinx.serialization.json.Json
import java.io.File
import javax.crypto.SecretKey

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

    protected inline fun <T> withEncryptedTicketsDirectory(
        session: StorageSession?,
        action: (StorageSession, File) -> T
    ): T? {
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
        storageSpec.detailExtension ?: return
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

