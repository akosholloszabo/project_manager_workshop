package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ProjectsStorage {
    private const val PROJECTS_FOLDER_NAME = "projects"
    private const val PROJECT_EXTENSION = ".json"
    private const val DETAILS_EXTENSION = ".md"
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)
    private val json = Json { encodeDefaults = true; prettyPrint = true }

    data class PersistedProject(val file: File, val project: Project)

    fun ensureProjectsDirectory(root: String?): File? {
        val folder = root?.let { File(it, PROJECTS_FOLDER_NAME) } ?: return null
        if (!folder.exists() && !folder.mkdirs()) return null
        return folder
    }

    fun loadProjects(root: String?): List<PersistedProject> {
        val folder = ensureProjectsDirectory(root) ?: return emptyList()
        return folder.listFiles { file ->
            file.isFile && file.extension.equals(PROJECT_EXTENSION.trimStart('.'), ignoreCase = true)
        }?.mapNotNull(::projectFromFile)?.sortedByDescending { it.file.lastModified() } ?: emptyList()
    }

    fun createProject(root: String?, name: String? = null, description: String = ""): PersistedProject? {
        val folder = ensureProjectsDirectory(root) ?: return null
        val baseName = sanitizeFileName(name ?: "project")
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val file = File(folder, "$baseName-$timestamp$PROJECT_EXTENSION")
        val defaultName = name?.takeIf { it.isNotBlank() } ?: "New project"
        val project = Project(0, defaultName, description)
        return runCatching {
            saveProject(project, file, "")
            projectFromFile(file)
        }.getOrNull()
    }

    fun saveProject(project: Project, file: File, details: String): Boolean {
        return runCatching {
            file.writeText(json.encodeToString(project))
            saveDetails(file, details)
            true
        }.getOrDefault(false)
    }

    fun deleteProject(file: File): Boolean {
        return runCatching {
            getDetailsFile(file).delete()
            file.delete()
        }.getOrDefault(false)
    }

    private fun projectFromFile(file: File): PersistedProject? {
        return runCatching {
            val content = file.readText()
            val parsed = json.decodeFromString<Project>(content)
            val normalized = parsed.copy(id = file.canonicalPath.hashCode(), details = loadDetails(file))
            PersistedProject(file, normalized)
        }.getOrNull()
    }

    private fun getDetailsFile(projectFile: File): File {
        val parent = projectFile.parentFile ?: projectFile
        return File(parent, projectFile.nameWithoutExtension + DETAILS_EXTENSION)
    }

    private fun ensureDetailsFile(projectFile: File): File {
        val detailsFile = getDetailsFile(projectFile)
        if (!detailsFile.exists()) {
            detailsFile.writeText("")
        }
        return detailsFile
    }

    private fun loadDetails(projectFile: File): String {
        return runCatching {
            ensureDetailsFile(projectFile).readText()
        }.getOrDefault("")
    }

    private fun saveDetails(projectFile: File, details: String) {
        val detailsFile = getDetailsFile(projectFile)
        detailsFile.writeText(details)
    }

    private fun sanitizeFileName(raw: String): String {
        val sanitized = raw.trim().ifEmpty { "project" }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .replace(Regex("\\s+"), "-")
        return sanitized.trim('-').ifEmpty { "project" }
    }
}
