package hu.akosholloszabo.project_manager.project_manager_workshop.repository

import hu.akosholloszabo.project_manager.project_manager_workshop.db.DatabaseFactory.dbQuery
import hu.akosholloszabo.project_manager.project_manager_workshop.db.ProjectsTable
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class ProjectRepositoryImpl : ProjectRepository {
    private fun ResultRow.toProject(): Project = Project(
        id = this[ProjectsTable.id].value,
        name = this[ProjectsTable.name],
        description = this[ProjectsTable.description],
        details = this[ProjectsTable.details]
    )

    override suspend fun getAll(): List<Project> = dbQuery {
        ProjectsTable.selectAll().map { it.toProject() }
    }

    override suspend fun getById(id: Int): Project? = dbQuery {
        ProjectsTable.select { ProjectsTable.id eq id }
            .map { it.toProject() }
            .singleOrNull()
    }

    override suspend fun create(payload: ProjectPayload): Project = dbQuery {
        val insertedId = ProjectsTable.insertAndGetId {
            it[name] = payload.name
            it[description] = payload.description
            it[details] = payload.details
        }.value

        ProjectsTable.select { ProjectsTable.id eq insertedId }
            .map { it.toProject() }
            .single()
    }

    override suspend fun update(id: Int, payload: ProjectPayload): Boolean = dbQuery {
        ProjectsTable.update({ ProjectsTable.id eq id }) {
            it[name] = payload.name
            it[description] = payload.description
            it[details] = payload.details
        } > 0
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        ProjectsTable.deleteWhere { ProjectsTable.id eq id } > 0
    }
}

