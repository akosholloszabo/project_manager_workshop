package hu.akosholloszabo.project_manager.project_manager_workshop.service

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectRepository

interface ProjectService {
    suspend fun getAll(): List<Project>
    suspend fun getById(id: Int): Project?
    suspend fun create(payload: ProjectPayload): Project
    suspend fun update(id: Int, payload: ProjectPayload): Boolean
    suspend fun delete(id: Int): Boolean
}

class ProjectServiceImpl(private val repository: ProjectRepository) : ProjectService {
    override suspend fun getAll(): List<Project> = repository.getAll()
    override suspend fun getById(id: Int): Project? = repository.getById(id)
    override suspend fun create(payload: ProjectPayload): Project = repository.create(payload)
    override suspend fun update(id: Int, payload: ProjectPayload): Boolean = repository.update(id, payload)
    override suspend fun delete(id: Int): Boolean = repository.delete(id)
}

