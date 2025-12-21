package hu.akosholloszabo.project_manager.project_manager_workshop.model

data class TicketCardState(
    val persisted: Persisted<Ticket>,
    val projectName: String,
    val isSelected: Boolean
)
