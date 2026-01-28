package hu.akosholloszabo.project_manager.project_manager_workshop.model

import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_backlog
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_completed
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_in_processing
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_in_refinement
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_in_testing
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_ready_for_processing
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_ready_for_refinement
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_ready_for_testing
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.ticket_status_rejected
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
enum class TicketStatus(val resId: StringResource) {
    //TODO capitalize names
    Backlog(Res.string.ticket_status_backlog),
    ReadyForRefinement(Res.string.ticket_status_ready_for_refinement),
    InRefinement(Res.string.ticket_status_in_refinement),
    ReadyForProcessing(Res.string.ticket_status_ready_for_processing),
    InProcessing(Res.string.ticket_status_in_processing),
    ReadyForTesting(Res.string.ticket_status_ready_for_testing),
    InTesting(Res.string.ticket_status_in_testing),
    Completed(Res.string.ticket_status_completed),
    Rejected(Res.string.ticket_status_rejected);

    val displayName: String
        get() = getStringResource(resId)

    companion object {
        fun fromName(name: String): TicketStatus = entries
            .firstOrNull { it.displayName == name || it.name == name }
            ?: throw Exception("No TicketStatus with name: $name")
    }
}
