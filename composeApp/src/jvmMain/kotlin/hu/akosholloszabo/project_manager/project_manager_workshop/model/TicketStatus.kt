package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

@Serializable
enum class TicketStatus {
    Backlog,
    ReadyForRefinement,
    InRefinement,
    ReadyForProcessing,
    InProcessing,
    ReadyForTesting,
    InTesting,
    Completed,
    Rejected;

    companion object {
        val default: TicketStatus = Backlog
        fun fromName(name: String): TicketStatus = entries.firstOrNull { it.name == name } ?: default
    }
}
