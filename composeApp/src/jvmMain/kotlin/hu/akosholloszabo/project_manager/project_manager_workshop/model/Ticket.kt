package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
/**
 * Ticket model
 */
data class Ticket(
    val id: Int,
    val title: String,
    val projectId: Int,
    val status: TicketStatus?,
    @Transient
    val details: String = ""
)
