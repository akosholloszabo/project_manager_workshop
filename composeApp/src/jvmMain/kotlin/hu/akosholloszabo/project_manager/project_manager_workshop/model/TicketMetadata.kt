package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

@Serializable
data class TicketMetadata(
    val id: Int,
    val title: String,
    val projectId: Int,
    val status: String
)

