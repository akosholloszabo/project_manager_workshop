package hu.akosholloszabo.project_manager.project_manager_workshop.repository

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project

interface ProjectRepository {
    suspend fun getAll(): List<Project>
    suspend fun getById(id: Int): Project?
    suspend fun create(payload: ProjectPayload): Project
    suspend fun update(id: Int, payload: ProjectPayload): Boolean
    suspend fun delete(id: Int): Boolean
}

@kotlinx.serialization.Serializable
data class ProjectPayload(
    val name: String,
    val description: String,
    val details: String = ""
)

