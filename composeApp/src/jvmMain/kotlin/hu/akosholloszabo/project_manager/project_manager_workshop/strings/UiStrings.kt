package hu.akosholloszabo.project_manager.project_manager_workshop.strings

import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import org.koin.core.component.KoinComponent
import java.text.MessageFormat

class UiStrings: KoinComponent {

    val projectsTitle: String get() = getKoin().getProperty("projects.title","")
     val projectsNewLabel: String get() = getKoin().getProperty("projects.new","")
     val projectNameFieldLabel: String get() = getKoin().getProperty("projects.field.name","")
     val projectDescriptionFieldLabel: String get() = getKoin().getProperty("projects.field.description","")
     val projectDetailsFieldLabel: String get() = getKoin().getProperty("projects.field.details","")
     val projectsEmptyMessage: String get() = getKoin().getProperty("projects.empty.message","")
     val projectsEmptyDescription: String get() = getKoin().getProperty("projects.empty.description","")
     val projectsDetailsEmpty: String get() = getKoin().getProperty("projects.details.empty","")

     val ticketsTitle: String get() = getKoin().getProperty("tickets.title","")
     val ticketsNewLabel: String get() = getKoin().getProperty("tickets.new","")
     val ticketDetailsTitle: String get() = getKoin().getProperty("tickets.details.title","")
     val ticketBackLabel: String get() = getKoin().getProperty("tickets.back","")
     val ticketNoTicketsYet: String get() = getKoin().getProperty("tickets.no.items","")
     val ticketNoDetails: String get() = getKoin().getProperty("tickets.details.empty","")

     val ticketTitleFieldLabel: String get() = getKoin().getProperty("tickets.field.title","")
     val ticketProjectFieldLabel: String get() = getKoin().getProperty("tickets.field.project","")
     val ticketStatusFieldLabel: String get() = getKoin().getProperty("tickets.field.status","")
     val ticketDetailsFieldLabel: String get() = getKoin().getProperty("tickets.field.details","")
     val ticketEditorNoProjectLabel: String get() = getKoin().getProperty("tickets.editor.no.project","")

     val crudNewLabel: String get() = getKoin().getProperty("crud.new","")

    fun ticketStatusHeader(status: TicketStatus): String =
        MessageFormat.format(getKoin().getProperty("ticket.status.header", ""),
            getKoin().getProperty("ticket.status.${status.name}", "")
        )
     fun ticketProjectHeader(projectName: String): String =
        MessageFormat.format(getKoin().getProperty("ticket.project.header", ""), projectName)
}

