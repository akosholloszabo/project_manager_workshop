package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSpec
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.*
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import kotlinx.serialization.json.Json
import java.io.File

abstract class LocalProjectsStorage(
    val fileStorageHelper: FileStorageHelper
) : ProjectsStorage {

    protected val storageSpec = StorageSpec(
        folderName = "projects",
        primaryExtension = ".json",
        fallbackName = "project",
        detailExtension = ".md"
    )

    protected val json: Json = fileStorageHelper.defaultJson

    protected fun <T> withProjectsDirectory(session: StorageSession?, action: (File) -> T): T {
        require(session != null) { getStringResource(Res.string.storage_session_required) }
        val folder = session.let { fileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
        require(folder != null) { getStringResource(Res.string.storage_folder_access_error) }
        return action(folder)
    }

    protected fun <T> withEncryptedProjectsDirectory(
        session: StorageSession,
        action: (StorageSession, File) -> T
    ): T {
        // TODO why is this here
        val current = session
        val folder = File(current.folderPath, storageSpec.folderName)
            .takeIf { it.exists() || it.mkdirs() }
            ?: throw Exception(getStringResource(Res.string.storage_folder_access_error))
        return action(current, folder)
    }

    protected fun readDetails(file: File): String {
        val extension = storageSpec.detailExtension ?: return ""
        return runCatching {
            fileStorageHelper.getSidecarFile(file, extension).takeIf(File::exists)?.readText().orEmpty()
        }.getOrDefault("")
    }

    protected fun writeDetails(file: File, details: String) {
        fileStorageHelper.writeDetails(file, storageSpec, details)
    }

    protected inline fun <T> safe(block: () -> T): T = run(block)

    protected fun projectFromFile(file: File): Persisted<Project> {
        // TODO {} replace with =
        return safe {
            val content = file.readText()
            val parsed = json.decodeFromString<Project>(content)
            val normalized = parsed.copy(details = readDetails(file))
            Persisted(file, normalized)
        }
    }
}
