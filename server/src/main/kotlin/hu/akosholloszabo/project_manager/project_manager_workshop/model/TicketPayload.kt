package hu.akosholloszabo.project_manager.project_manager_workshop.model

import hu.akosholloszabo.project_manager.project_manager_workshop.entity.TicketStatus
import kotlinx.serialization.Serializable

@Serializable
data class TicketPayload(
    val title: String,
    val projectId: Int,
    val status: TicketStatus,
    val details: String
)
