package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

@Serializable
/**
 * Data transfer object that mirrors the server's ticket payload structure.
 */
data class TicketPayload(
    val title: String,
    val projectId: Int,
    val status: TicketStatus?,
    val details: String = ""
)

