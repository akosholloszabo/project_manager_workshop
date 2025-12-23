package hu.akosholloszabo.project_manager.project_manager_workshop.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object ProjectsTable : IntIdTable("projects") {
    val name: Column<String> = varchar("name", 256)
    val description: Column<String> = text("description")
    val details: Column<String> = text("details").default("")
}
