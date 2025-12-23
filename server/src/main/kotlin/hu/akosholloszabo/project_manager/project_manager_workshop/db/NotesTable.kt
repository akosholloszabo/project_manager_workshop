package hu.akosholloszabo.project_manager.project_manager_workshop.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object NotesTable : IntIdTable("notes") {
    val title: Column<String> = varchar("title", 256)
    val content: Column<String> = text("content")
}

