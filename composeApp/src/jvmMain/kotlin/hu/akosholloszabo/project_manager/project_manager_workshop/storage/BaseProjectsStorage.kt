package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import kotlinx.serialization.json.Json
import java.io.File
import javax.crypto.SecretKey

abstract class BaseProjectsStorage : ProjectsStorage {
    protected val storageSpec = FileStorageHelper.StorageSpec(
        folderName = "projects",
        primaryExtension = ".json",
        fallbackName = "project",
        detailExtension = ".md"
    )

    protected val json: Json = FileStorageHelper.defaultJson

    protected inline fun <T> withProjectsDirectory(session: StorageSession?, action: (File) -> T): T? {
        val folder = session?.let { FileStorageHelper.ensureStorageDirectory(it.folderPath, storageSpec) }
            ?: return null
        return action(folder)
    }

    protected inline fun <T> withEncryptedProjectsDirectory(
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
        return runCatching {
            FileStorageHelper.getSidecarFile(file, extension).takeIf(File::exists)?.readText().orEmpty()
        }.getOrDefault("")
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

    protected fun projectFromFile(file: File): Persisted<Project>? {
        return safe {
            val content = file.readText()
            val parsed = json.decodeFromString<Project>(content)
            val normalized = parsed.copy(details = readDetails(file))
            Persisted(file, normalized)
        }
    }

    protected fun projectFromFileEncrypted(file: File, key: SecretKey): Persisted<Project>? {
        return safe {
            val encrypted = file.readText()
            val payload = StorageCipher.tryDecrypt(encrypted, key) ?: return null
            val parsed = json.decodeFromString<Project>(payload)
            val normalized = parsed.copy(details = readDetailsEncrypted(file, key))
            Persisted(file, normalized)
        }
    }
}

