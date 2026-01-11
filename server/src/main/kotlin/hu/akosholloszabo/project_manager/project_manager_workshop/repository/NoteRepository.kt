package hu.akosholloszabo.project_manager.project_manager_workshop.repository

import hu.akosholloszabo.project_manager.project_manager_workshop.db.DatabaseFactory.dbQuery
import hu.akosholloszabo.project_manager.project_manager_workshop.db.NotesTable
import hu.akosholloszabo.project_manager.project_manager_workshop.entity.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.NotePayload
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class NoteRepository {
    private fun ResultRow.toNote(): Note = Note(
        id = this[NotesTable.id].value,
        title = this[NotesTable.title],
        content = this[NotesTable.content]
    )

    suspend fun getAll(): List<Note> = dbQuery {
        NotesTable.selectAll().map { it.toNote() }
    }

    suspend fun getById(id: Int): Note? = dbQuery {
        NotesTable.select { NotesTable.id eq id }
            .map { it.toNote() }
            .singleOrNull()
    }

    suspend fun create(payload: NotePayload): Note = dbQuery {
        val insertedId = NotesTable.insertAndGetId {
            it[title] = payload.title
            it[content] = payload.content
        }.value

        NotesTable.select { NotesTable.id eq insertedId }
            .map { it.toNote() }
            .single()
    }

    suspend fun update(id: Int, payload: NotePayload): Boolean = dbQuery {
        NotesTable.update({ NotesTable.id eq id }) {
            it[title] = payload.title
            it[content] = payload.content
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        NotesTable.deleteWhere { NotesTable.id eq id } > 0
    }
}


