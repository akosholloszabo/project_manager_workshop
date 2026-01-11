package hu.akosholloszabo.project_manager.project_manager_workshop.entity

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
}
