package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.crypto.SecretKey

interface ProjectsStorage {
    fun loadProjects(session: StorageSession?): List<Persisted<Project>>
    fun createProject(session: StorageSession?, name: String? = null, description: String = ""): Persisted<Project>?
    fun saveProject(session: StorageSession?, project: Project, file: File, details: String): Boolean
    fun deleteProject(session: StorageSession?, file: File): Boolean
}

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

    protected inline fun <T> withEncryptedProjectsDirectory(session: StorageSession?, action: (StorageSession, File) -> T): T? {
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

class PlainProjectsStorage : BaseProjectsStorage() {
    override fun loadProjects(session: StorageSession?): List<Persisted<Project>> {
        return withProjectsDirectory(session) { folder ->
            FileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull(::projectFromFile)
        } ?: emptyList()
    }

    override fun createProject(session: StorageSession?, name: String?, description: String): Persisted<Project>? {
        return withProjectsDirectory(session) { folder ->
            val file = FileStorageHelper.createTimestampedFile(folder, name, storageSpec)
            val defaultName = name?.takeIf { it.isNotBlank() } ?: "New project"
            val project = Project(EntityIdGenerator.newId(), defaultName, description)
            safe {
                saveProject(session, project, file, "")
                projectFromFile(file)
            }
        }
    }

    override fun saveProject(session: StorageSession?, project: Project, file: File, details: String): Boolean {
        if (session == null) return false
        return safe {
            file.writeText(json.encodeToString(project))
            writeDetails(file, details)
            true
        } ?: false
    }

    override fun deleteProject(session: StorageSession?, file: File): Boolean {
        return session?.let {
            safe {
                FileStorageHelper.deleteDetails(file, storageSpec)
                file.delete()
            }
        } ?: false
    }
}

class EncryptedProjectsStorage : BaseProjectsStorage() {
    override fun loadProjects(session: StorageSession?): List<Persisted<Project>> {
        return withEncryptedProjectsDirectory(session) { current, folder ->
            val key = current.encryptionKey ?: return@withEncryptedProjectsDirectory emptyList()
            FileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull { projectFromFileEncrypted(it, key) }
        } ?: emptyList()
    }

    override fun createProject(session: StorageSession?, name: String?, description: String): Persisted<Project>? {
        return withEncryptedProjectsDirectory(session) { current, folder ->
            val key = current.encryptionKey ?: return@withEncryptedProjectsDirectory null
            val file = FileStorageHelper.createTimestampedFile(folder, name, storageSpec)
            val defaultName = name?.takeIf { it.isNotBlank() } ?: "New project"
            val project = Project(EntityIdGenerator.newId(), defaultName, description)
            safe {
                saveProject(session, project, file, "")
                projectFromFileEncrypted(file, key)
            }
        }
    }

    override fun saveProject(session: StorageSession?, project: Project, file: File, details: String): Boolean {
        val key = session?.encryptionKey ?: return false
        return safe {
            file.writeText(StorageCipher.encrypt(json.encodeToString(project), key))
            writeDetailsEncrypted(file, key, details)
            true
        } ?: false
    }

    override fun deleteProject(session: StorageSession?, file: File): Boolean {
        return session?.let {
            safe {
                FileStorageHelper.deleteDetails(file, storageSpec)
                file.delete()
            }
        } ?: false
    }
}
