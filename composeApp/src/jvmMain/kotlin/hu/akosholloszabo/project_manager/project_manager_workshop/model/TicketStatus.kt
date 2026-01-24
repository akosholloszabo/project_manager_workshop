package hu.akosholloszabo.project_manager.project_manager_workshop.model


import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.KoinUtilities.getText
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

@Serializable
enum class TicketStatus : KoinComponent {
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
        get() = getText("ticket.status.${name}")


    companion object {
        fun fromName(name: String): TicketStatus = entries
            .firstOrNull { it.displayName == name || it.name == name }
            ?: throw Exception("No TicketStatus with name: $name")
    }
}
