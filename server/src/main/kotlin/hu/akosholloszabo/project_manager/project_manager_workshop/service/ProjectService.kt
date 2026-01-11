package hu.akosholloszabo.project_manager.project_manager_workshop.service

import hu.akosholloszabo.project_manager.project_manager_workshop.entity.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.ProjectPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectRepository

class ProjectService(private val repository: ProjectRepository) {
    suspend fun getAll(): List<Project> = repository.getAll()
    suspend fun getById(id: Int): Project? = repository.getById(id)
    suspend fun create(payload: ProjectPayload): Project = repository.create(payload)
    suspend fun update(id: Int, payload: ProjectPayload): Boolean = repository.update(id, payload)
    suspend fun delete(id: Int): Boolean = repository.delete(id)
}

