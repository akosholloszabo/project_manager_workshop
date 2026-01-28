package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.*
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import java.io.File
import java.util.UUID.randomUUID

class PlainProjectsStorage(
    fileStorageHelper: FileStorageHelper
) : LocalProjectsStorage(fileStorageHelper) {
    override fun loadProjects(session: StorageSession?): List<Persisted<Project>> {
        // TODO {} replace with =
        return withProjectsDirectory(session) { folder ->
            fileStorageHelper.listStorageFiles(folder, storageSpec)
                .map(::projectFromFile)
        }
    }

    override fun createProject(
        session: StorageSession?,
        name: String,
        description: String,
        details: String
    ): Persisted<Project> {
        // TODO {} replace with =
        return withProjectsDirectory(session) { folder ->
            val file = fileStorageHelper.createTimestampedFile(folder, name, storageSpec)
            val defaultName = name.takeIf { it.isNotBlank() }
                ?: getStringResource(Res.string.project_default_name)
            val project = Project(randomUUID().hashCode(), defaultName, description)
            safe {
                saveProject(session, project, file, details)
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
}
