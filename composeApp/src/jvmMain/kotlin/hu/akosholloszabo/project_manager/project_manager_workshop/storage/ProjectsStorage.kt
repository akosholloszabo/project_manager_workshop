package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import kotlinx.serialization.encodeToString
import java.io.File

object ProjectsStorage {
    private val storageSpec = FileStorageHelper.StorageSpec(
        folderName = "projects",
        primaryExtension = ".json",
        fallbackName = "project",
        detailExtension = ".md"
    )

    private val json = FileStorageHelper.defaultJson

    fun ensureProjectsDirectory(root: String?): File? {
        return FileStorageHelper.ensureStorageDirectory(root, storageSpec)
    }

    fun loadProjects(root: String?): List<Persisted<Project>> {
        return withProjectsDirectory(root) { folder ->
            FileStorageHelper.listStorageFiles(folder, storageSpec)
                .mapNotNull(::projectFromFile)
        } ?: emptyList()
    }

    fun createProject(root: String?, name: String? = null, description: String = ""): Persisted<Project>? {
        return withProjectsDirectory(root) { folder ->
            val file = FileStorageHelper.createTimestampedFile(folder, name, storageSpec)
            val defaultName = name?.takeIf { it.isNotBlank() } ?: "New project"
            val project = Project(EntityIdGenerator.newId(), defaultName, description)
            safe {
                saveProject(project, file, "")
                projectFromFile(file)
            }
        }
    }

    fun saveProject(project: Project, file: File, details: String): Boolean {
        return safe {
            file.writeText(json.encodeToString(project))
            FileStorageHelper.writeDetails(file, storageSpec, details)
            true
        } ?: false
    }

    fun deleteProject(file: File): Boolean {
        return safe {
            FileStorageHelper.deleteDetails(file, storageSpec)
            file.delete()
        } ?: false
    }

    private fun projectFromFile(file: File): Persisted<Project>? {
        return safe {
            val content = file.readText()
            val parsed = json.decodeFromString<Project>(content)
            val normalized = parsed.copy(
                details = FileStorageHelper.readDetails(file, storageSpec)
            )
            Persisted(file, normalized)
        }
    }

    private inline fun <T> withProjectsDirectory(root: String?, action: (File) -> T): T? {
        val folder = ensureProjectsDirectory(root) ?: return null
        return action(folder)
    }

    private inline fun <T> safe(block: () -> T): T? = runCatching(block).getOrNull()
}
