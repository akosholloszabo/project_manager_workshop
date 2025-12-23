package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketStore(
    private val workingFolderStore: WorkingFolderStore,
    private val ticketsStorage: TicketsStorage
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _tickets = MutableStateFlow<List<Persisted<Ticket>>>(emptyList())
    val tickets: StateFlow<List<Persisted<Ticket>>> = _tickets.asStateFlow()

    init {
        scope.launch {
            workingFolderStore.session.collectLatest { session ->
                refreshTicketsInternal(session)
            }
        }
    }

    fun refresh() {
        refreshTickets()
    }

    fun createTicket(): Persisted<Ticket>? {
        val session = workingFolderStore.session.value ?: return null
        val created = ticketsStorage.createTicket(session)
        if (created != null) {
            refreshTickets()
        }
        return created
    }

    fun saveTicket(persisted: Persisted<Ticket>, draft: Ticket): Boolean {
        val session = workingFolderStore.session.value ?: return false
        val success = ticketsStorage.saveTicket(session, draft, persisted.file, draft.details)
        if (success) {
            refreshTickets()
        }
        return success
    }

    fun deleteTicket(persisted: Persisted<Ticket>): Boolean {
        val session = workingFolderStore.session.value ?: return false
        val success = ticketsStorage.deleteTicket(session, persisted.file)
        if (success) {
            refreshTickets()
        }
        return success
    }

    private fun refreshTickets() {
        scope.launch {
            refreshTicketsInternal(workingFolderStore.session.value)
        }
    }

    private suspend fun refreshTicketsInternal(session: StorageSession?) {
        val loaded = withContext(Dispatchers.IO) {
            ticketsStorage.loadTickets(session)
        }
        _tickets.emit(loaded)
    }
}
