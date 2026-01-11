package hu.akosholloszabo.project_manager.project_manager_workshop.model

import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Texts
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

    val displayName: String
        get() = Texts.get("ticket.status.${name}") ?: name


    companion object {
        fun fromName(name: String): TicketStatus = entries
            .firstOrNull { it.displayName == name || it.name == name }
            ?: throw Exception("No TicketStatus with name: $name")
    }
}
