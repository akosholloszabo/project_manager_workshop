package hu.akosholloszabo.project_manager.project_manager_workshop.model

data class TicketColumnState(
    val status: TicketStatus,
    val cards: List<TicketCardState>
)
