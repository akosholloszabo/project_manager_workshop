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
        val folder = ensureProjectsDirectory(root) ?: return emptyList()
        return FileStorageHelper.listStorageFiles(folder, storageSpec)
            .mapNotNull(::projectFromFile)
    }

    fun createProject(root: String?, name: String? = null, description: String = ""): Persisted<Project>? {
        val folder = ensureProjectsDirectory(root) ?: return null
        val file = FileStorageHelper.createTimestampedFile(folder, name, storageSpec)
        val defaultName = name?.takeIf { it.isNotBlank() } ?: "New project"
        val project = Project(EntityIdGenerator.newId(), defaultName, description)
        return runCatching {
            saveProject(project, file, "")
            projectFromFile(file)
        }.getOrNull()
    }

    fun saveProject(project: Project, file: File, details: String): Boolean {
        return runCatching {
            file.writeText(json.encodeToString(project))
            FileStorageHelper.writeDetails(file, storageSpec, details)
            true
        }.getOrDefault(false)
    }

    fun deleteProject(file: File): Boolean {
        return runCatching {
            FileStorageHelper.deleteDetails(file, storageSpec)
            file.delete()
        }.getOrDefault(false)
    }

    private fun projectFromFile(file: File): Persisted<Project>? {
        return runCatching {
            val content = file.readText()
            val parsed = json.decodeFromString<Project>(content)
            val normalized = parsed.copy(
                details = FileStorageHelper.readDetails(file, storageSpec)
            )
            Persisted<Project>(file, normalized)
        }.getOrNull()
    }
}
