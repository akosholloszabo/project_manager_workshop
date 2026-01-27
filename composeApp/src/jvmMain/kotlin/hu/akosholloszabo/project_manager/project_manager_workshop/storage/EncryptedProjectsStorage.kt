package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText

import java.io.File
import java.util.UUID.randomUUID
import javax.crypto.SecretKey

class EncryptedProjectsStorage(
    val storageCipher: StorageCipher,
    fileStorageHelper: FileStorageHelper,
) : LocalProjectsStorage(fileStorageHelper) {

    override fun loadProjects(session: StorageSession?): List<Persisted<Project>> {
        require(session != null) { getText("storage.session.required") }
        return withEncryptedProjectsDirectory(session) { current, folder ->
            val key = current.encryptionKey ?: return@withEncryptedProjectsDirectory emptyList()
            fileStorageHelper.listStorageFiles(folder, storageSpec)
                .map { projectFromFileEncrypted(it, key) }
        }
    }

    override fun createProject(
        session: StorageSession?,
        name: String,
        description: String,
        details: String
    ): Persisted<Project> {
        require(session != null) { getText("storage.session.required") }
        return withEncryptedProjectsDirectory(session) { current, folder ->
            require(current.encryptionKey != null) { getText("storage.session.encryption.required") }
            val key = current.encryptionKey
            val file = fileStorageHelper.createTimestampedFile(folder, name, storageSpec)
            val defaultName = name.takeIf { it.isNotBlank() } ?: getText("projects.new")
            val project = Project(randomUUID().hashCode(), defaultName, description)
            safe {
                saveProject(session, project, file, details)
                projectFromFileEncrypted(file, key)
            }
        }
    }

    override fun saveProject(session: StorageSession?, project: Project, file: File, details: String): Boolean {
        // TODO takeIf/takeUnless
        val key = session?.encryptionKey ?: return false
        return safe {
            file.writeText(storageCipher.encrypt(json.encodeToString(project), key))
            writeDetailsEncrypted(file, key, details)
            true
        }
    }

    override fun deleteProject(session: StorageSession?, file: File): Boolean {
        // TODO {} replace with =
        return session?.let {
            safe {
                fileStorageHelper.deleteDetails(file, storageSpec)
                file.delete()
            }
        } ?: false
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

    private fun projectFromFileEncrypted(file: File, key: SecretKey): Persisted<Project> {
        // TODO {} replace with =
        return safe {
            val encrypted = file.readText()
            val payload =
                storageCipher.tryDecrypt(encrypted, key) ?: throw Exception(getText("storage.encrypted.decrypt_error"))
            val parsed = json.decodeFromString<Project>(payload)
            val normalized = parsed.copy(details = readDetailsEncrypted(file, key))
            Persisted(file, normalized)
        }
    }
}
