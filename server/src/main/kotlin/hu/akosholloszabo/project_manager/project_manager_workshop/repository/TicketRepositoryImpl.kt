package hu.akosholloszabo.project_manager.project_manager_workshop.repository

import hu.akosholloszabo.project_manager.project_manager_workshop.db.DatabaseFactory.dbQuery
import hu.akosholloszabo.project_manager.project_manager_workshop.db.TicketsTable
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class TicketRepositoryImpl : TicketRepository {
    private fun ResultRow.toTicket(): Ticket = Ticket(
        id = this[TicketsTable.id].value,
        title = this[TicketsTable.title],
        projectId = this[TicketsTable.projectId],
        status = this[TicketsTable.status],
        details = this[TicketsTable.details]
    )

    override suspend fun getAll(): List<Ticket> = dbQuery {
        TicketsTable.selectAll().map { it.toTicket() }
    }

    override suspend fun getById(id: Int): Ticket? = dbQuery {
        TicketsTable.select { TicketsTable.id eq id }
            .map { it.toTicket() }
            .singleOrNull()
    }

    override suspend fun create(payload: TicketPayload): Ticket = dbQuery {
        val insertedId = TicketsTable.insertAndGetId {
            it[title] = payload.title
            it[projectId] = payload.projectId
            it[status] = payload.status
            it[details] = payload.details
        }.value

        TicketsTable.select { TicketsTable.id eq insertedId }
            .map { it.toTicket() }
            .single()
    }

    override suspend fun update(id: Int, payload: TicketPayload): Boolean = dbQuery {
        TicketsTable.update({ TicketsTable.id eq id }) {
            it[title] = payload.title
            it[projectId] = payload.projectId
            it[status] = payload.status
            it[details] = payload.details
        } > 0
    }

    override suspend fun delete(id: Int): Boolean = dbQuery {
        TicketsTable.deleteWhere { TicketsTable.id eq id } > 0
    }
}

