package hu.akosholloszabo.project_manager.project_manager_workshop.model

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage

data class TicketCardState(
    val persisted: TicketsStorage.PersistedTicket,
    val projectName: String,
    val isSelected: Boolean
)
