package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketStore(
    private val workingFolder: String?,
    private val ticketsStorage: TicketsStorage
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private val _tickets = MutableStateFlow<List<Persisted<Ticket>>>(emptyList())
    val tickets: StateFlow<List<Persisted<Ticket>>> = _tickets.asStateFlow()

    init {
        refreshTickets()
    }

    fun refresh() {
        refreshTickets()
    }

    fun createTicket(): Persisted<Ticket>? {
        val created = ticketsStorage.createTicket(workingFolder)
        if (created != null) {
            refreshTickets()
        }
        return created
    }

    fun saveTicket(persisted: Persisted<Ticket>, draft: Ticket): Boolean {
        val success = ticketsStorage.saveTicket(draft, persisted.file, draft.details)
        if (success) {
            refreshTickets()
        }
        return success
    }

    fun deleteTicket(persisted: Persisted<Ticket>): Boolean {
        val success = ticketsStorage.deleteTicket(persisted.file)
        if (success) {
            refreshTickets()
        }
        return success
    }

    private fun refreshTickets() {
        scope.launch {
            val loaded = ticketsStorage.loadTickets(workingFolder)
            _tickets.emit(loaded)
        }
    }
}
