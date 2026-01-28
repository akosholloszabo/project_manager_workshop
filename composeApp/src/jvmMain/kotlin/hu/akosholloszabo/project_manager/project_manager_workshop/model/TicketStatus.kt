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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import java.util.*

@Serializable
enum class TicketStatus(val resId: StringResource) {
    @SerialName("Backlog")
    BACKLOG(Res.string.ticket_status_backlog),

    @SerialName("ReadyForRefinement")
    READY_FOR_REFINEMENT(Res.string.ticket_status_ready_for_refinement),

    @SerialName("InRefinement")
    IN_REFINEMENT(Res.string.ticket_status_in_refinement),

    @SerialName("ReadyForProcessing")
    READY_FOR_PROCESSING(Res.string.ticket_status_ready_for_processing),

    @SerialName("InProcessing")
    IN_PROCESSING(Res.string.ticket_status_in_processing),

    @SerialName("ReadyForTesting")
    READY_FOR_TESTING(Res.string.ticket_status_ready_for_testing),

    @SerialName("InTesting")
    IN_TESTING(Res.string.ticket_status_in_testing),

    @SerialName("Completed")
    COMPLETED(Res.string.ticket_status_completed),

    @SerialName("Rejected")
    REJECTED(Res.string.ticket_status_rejected);

    companion object {
        fun fromName(name: String): TicketStatus = entries
            .firstOrNull { getStringResource(it.resId) == name || it.name == name.uppercase(Locale.getDefault()) }
            ?: throw Exception("No TicketStatus with name: $name")
    }
}
