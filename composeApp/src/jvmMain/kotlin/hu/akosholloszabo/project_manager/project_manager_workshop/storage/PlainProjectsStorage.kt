package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import java.io.File

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

