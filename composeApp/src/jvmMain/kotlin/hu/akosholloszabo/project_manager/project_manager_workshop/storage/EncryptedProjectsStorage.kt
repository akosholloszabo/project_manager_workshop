package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import java.io.File

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

