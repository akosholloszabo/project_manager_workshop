package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import java.io.File

interface ProjectsStorage {
    fun loadProjects(session: StorageSession?): List<Persisted<Project>>
    fun createProject(session: StorageSession?, name: String? = null, description: String = ""): Persisted<Project>?
    fun saveProject(session: StorageSession?, project: Project, file: File, details: String): Boolean
    fun deleteProject(session: StorageSession?, file: File): Boolean
}
