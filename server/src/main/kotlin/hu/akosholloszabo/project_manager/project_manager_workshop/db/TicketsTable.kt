package hu.akosholloszabo.project_manager.project_manager_workshop.db

import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object TicketsTable : IntIdTable("tickets") {
    val title: Column<String> = varchar("title", 256)
    val projectId: Column<Int> = integer("project_id")
    val status: Column<TicketStatus> = enumerationByName("status", 32, TicketStatus::class)
    val details: Column<String> = text("details").default("")
}

