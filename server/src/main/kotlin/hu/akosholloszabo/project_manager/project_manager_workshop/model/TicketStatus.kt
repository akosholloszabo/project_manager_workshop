package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

@Serializable
enum class TicketStatus(val displayText: String) {
    Backlog("Backlog"),
    ReadyForRefinement("Ready for refinement"),
    InRefinement("In refinement"),
    ReadyForProcessing("Ready for processing"),
    InProcessing("In processing"),
    ReadyForTesting("Ready for testing"),
    InTesting("In testing"),
    Completed("Completed"),
    Rejected("Rejected");

    companion object {
        val default: TicketStatus = Backlog
        fun fromDisplay(display: String): TicketStatus = entries.firstOrNull { it.displayText == display } ?: default
    }
}
