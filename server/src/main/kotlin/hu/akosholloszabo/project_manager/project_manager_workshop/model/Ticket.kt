package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

@Serializable
/**
 * Ticket model
 */
data class Ticket(
    val id: Int,
    val title: String,
    val projectId: Int,
    val status: TicketStatus,
    val details: String = ""
)
