package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.ProjectPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ProjectServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.projects_new
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import kotlinx.coroutines.runBlocking
import java.io.File

class ServerProjectsStorage(private val client: ProjectServerClient) : ProjectsStorage {
    override fun loadProjects(session: StorageSession?): List<Persisted<Project>> = runBlocking {
        client.getAll().map(::persistProject)
    }

    override fun createProject(
        session: StorageSession?,
        name: String,
        description: String,
        details: String
    ): Persisted<Project> {
        val payload = ProjectPayload(
            name = name.takeIf { it.isNotBlank() } ?: getStringResource(Res.string.projects_new),
            description = description,
            details = ""
        )
        return runBlocking {
            persistProject(client.create(payload))
        }
    }

    override fun saveProject(session: StorageSession?, project: Project, file: File, details: String): Boolean {
        return runBlocking {
            val payload = ProjectPayload(
                name = project.name,
                description = project.description,
                details = details
            )
            client.update(project.id, payload)
        }
    }

    override fun deleteProject(session: StorageSession?, file: File): Boolean {
        val id = extractId(file) ?: return false
        return runBlocking { client.delete(id) }
    }

    private fun persistProject(project: Project): Persisted<Project> = Persisted(projectFile(project.id), project)

    // TODO Why is this needed?
    private fun projectFile(id: Int): File = File("server-project-$id.json")

    private fun extractId(file: File): Int? = file.nameWithoutExtension
        .split('-')
        .lastOrNull()
        ?.toIntOrNull()
}
