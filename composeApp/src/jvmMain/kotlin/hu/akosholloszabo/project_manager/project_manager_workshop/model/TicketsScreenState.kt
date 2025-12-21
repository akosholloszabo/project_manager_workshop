package hu.akosholloszabo.project_manager.project_manager_workshop.model

data class TicketsScreenState(
    val columns: List<TicketColumnState> = emptyList(),
    val selectedTicket: Persisted<Ticket>? = null,
    val editorTicket: Ticket? = null,
    val isEditing: Boolean = false,
    val projects: List<Pair<Int, String>> = emptyList(),
    val boardVersion: Int = 0
)
