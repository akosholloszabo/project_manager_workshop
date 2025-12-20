package hu.akosholloszabo.project_manager.project_manager_workshop.component

import androidx.compose.runtime.saveable.Saver
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus


val TicketSaver: Saver<Ticket, Any> = Saver(
    save = { ticket ->
        listOf(ticket.id, ticket.title, ticket.projectId, ticket.status.name, ticket.details)
    },
    restore = { raw ->
        val data = raw as List<*>
        Ticket(
            id = data[0] as Int,
            title = data[1] as String,
            projectId = data[2] as Int,
            status = TicketStatus.valueOf(data[3] as String),
            details = data[4] as String
        )
    }
)
