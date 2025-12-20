package hu.akosholloszabo.project_manager.project_manager_workshop.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hu.akosholloszabo.project_manager.project_manager_workshop.actions.CrudAction
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage

internal class TicketBoardState(private val workingFolder: String) {
    private val projectsState = mutableStateListOf<Pair<Int, String>>()
    private val ticketsState = mutableStateListOf<TicketsStorage.PersistedTicket>()

    val projects: List<Pair<Int, String>>
        get() = projectsState

    var selectedTicket by mutableStateOf<TicketsStorage.PersistedTicket?>(null)
        private set

    private var pendingEditTicket by mutableStateOf<TicketsStorage.PersistedTicket?>(null)

    var ticketBoardVersion by mutableStateOf(0)
        private set

    val shouldStartEditingSelected: Boolean
        get() = selectedTicket?.file?.canonicalPath == pendingEditTicket?.file?.canonicalPath

    val columns: List<TicketColumnState>
        get() {
            val projectNames = projectsState.associate { it.first to it.second }
            val selectedPath = selectedTicket?.file?.canonicalPath
            return TicketStatus.entries.map { status ->
                TicketColumnState(
                    status = status,
                    cards = buildCardsFor(status, projectNames, selectedPath)
                )
            }
        }

    private fun buildCardsFor(
        status: TicketStatus,
        projectNames: Map<Int, String>,
        selectedPath: String?
    ): List<TicketCardState> =
        ticketsState
            .filter { it.ticket.status == status }
            .map { persisted ->
                TicketCardState(
                    persisted = persisted,
                    projectName = projectNames[persisted.ticket.projectId] ?: "No project",
                    isSelected = persisted.file.canonicalPath == selectedPath
                )
            }

    fun loadInitialData() {
        refreshProjects()
        refreshTickets()
    }

    fun handleAction(action: CrudAction) {
        when (action) {
            CrudAction.Create -> createNewTicket()
            CrudAction.Edit -> pendingEditTicket = selectedTicket
            CrudAction.Save -> selectedTicket?.let { current ->
                saveCurrentTicket(current.ticket)
                pendingEditTicket = null
            }

            CrudAction.Delete -> if (deleteCurrentTicket()) {
                pendingEditTicket = null
                selectedTicket = null
            }
        }
    }

    fun selectTicket(entry: TicketsStorage.PersistedTicket) {
        selectedTicket = entry
    }

    fun clearSelection() {
        selectedTicket = null
        pendingEditTicket = null
    }

    fun consumePendingEdit() {
        pendingEditTicket = null
    }

    fun onTicketSaved(@Suppress("UNUSED_PARAMETER") ticket: Ticket) {
        pendingEditTicket = null
    }

    fun saveCurrentTicket(draft: Ticket): Ticket? {
        val current = selectedTicket ?: return null
        val baseTicket = current.ticket
        val updated = baseTicket.copy(
            title = draft.title.trim().ifEmpty { baseTicket.title },
            projectId = draft.projectId,
            status = draft.status,
            details = draft.details
        )
        return if (TicketsStorage.saveTicket(updated, current.file, draft.details)) {
            refreshTickets(preserve = current)
            updated
        } else {
            null
        }
    }

    fun deleteCurrentTicket(): Boolean {
        val current = selectedTicket ?: return false
        return if (TicketsStorage.deleteTicket(current.file)) {
            refreshTickets()
            true
        } else {
            false
        }
    }

    private fun createNewTicket() {
        val created = TicketsStorage.createTicket(workingFolder) ?: return
        pendingEditTicket = created
        refreshTickets(preserve = created)
    }

    private fun refreshProjects() {
        val loadedProjects = ProjectsStorage
            .loadProjects(workingFolder)
            .map { it.project.id to it.project.name }
        projectsState.apply {
            clear()
            addAll(loadedProjects)
        }
    }

    private fun refreshTickets(preserve: TicketsStorage.PersistedTicket? = null) {
        val loaded = TicketsStorage.loadTickets(workingFolder)
        ticketsState.apply {
            clear()
            addAll(loaded)
        }
        selectedTicket = resolveTicketMatch(preserve ?: selectedTicket, loaded)
        pendingEditTicket = resolveTicketMatch(pendingEditTicket, loaded)
        ticketBoardVersion++
    }

    private fun resolveTicketMatch(
        target: TicketsStorage.PersistedTicket?,
        candidates: List<TicketsStorage.PersistedTicket>
    ): TicketsStorage.PersistedTicket? {
        target ?: return null
        return candidates.firstOrNull { it.file.canonicalPath == target.file.canonicalPath }
    }
}
