package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSpec
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText
import kotlinx.serialization.json.Json
import java.io.File

abstract class LocalProjectsStorage(val fileStorageHelper: FileStorageHelper) :
    ProjectsStorage {

    protected val storageSpec = StorageSpec(
        folderName = "projects",
        primaryExtension = ".json",
        fallbackName = "project",
        detailExtension = ".md"
    )

    protected val json: Json = fileStorageHelper.defaultJson

    protected inline fun <T> withProjectsDirectory(session: StorageSession?, action: (File) -> T): T {
        require(session != null) { getText("storage.session.required") }
        val folder = session.let { fileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
        require(folder != null) { getText("storage.folder.access_error") }
        return action(folder)
    }

    protected inline fun <T> withEncryptedProjectsDirectory(
        session: StorageSession,
        action: (StorageSession, File) -> T
    ): T {
        // TODO why is this here
        val current = session
        val folder = File(current.folderPath, storageSpec.folderName)
            .takeIf { it.exists() || it.mkdirs() }
            ?: throw Exception(getText("storage.folder.access_error"))
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
