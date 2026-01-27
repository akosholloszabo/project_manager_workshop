package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSpec
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketMetadata
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import kotlinx.serialization.json.Json
import java.io.File

// TODO shouldn't the parameter be private
abstract class LocalTicketsStorage(
    val fileStorageHelper: FileStorageHelper
    ) : TicketsStorage {

    protected val storageSpec = StorageSpec(
        folderName = "tickets",
        primaryExtension = ".json",
        fallbackName = "ticket",
        detailExtension = ".md"
    )

    protected val json: Json = fileStorageHelper.defaultJson

    protected inline fun <T> withTicketsDirectory(session: StorageSession?, action: (File) -> T): T? {
        val folder = session?.let { fileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
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
        return fileStorageHelper.getSidecarFile(file, extension).takeIf(File::exists)?.readText().orEmpty()
    }

    protected fun writeDetails(file: File, details: String) {
        storageSpec.detailExtension ?: return
        fileStorageHelper.writeDetails(file, storageSpec, details)
    }

    // TODO I did not see something like this, could be ok but is it needed?
    protected inline fun <T> safe(block: () -> T): T? = runCatching(block).getOrNull()

    protected fun ticketFromFile(file: File): Persisted<Ticket>? {
        // TODO {} replace with =
        return safe {
            val content = file.readText()
            val parsed = json.decodeFromString<TicketMetadata>(content)
            val normalized = Ticket(
                id = parsed.id,
                title = parsed.title,
                projectId = parsed.projectId,
                status = parsed.status?.let { TicketStatus.fromName(it) },
                details = readDetails(file)
            )
            Persisted(file, normalized)
        }
    }
}
