package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketStore(
    private val workingFolder: String?,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = coroutineScope

    private val _tickets = MutableStateFlow<List<Persisted<Ticket>>>(emptyList())
    val tickets: StateFlow<List<Persisted<Ticket>>> = _tickets.asStateFlow()

    init {
        scope.launch { refreshTickets() }
    }

    fun refresh() {
        scope.launch { refreshTickets() }
    }

    private suspend fun refreshTickets() {
        val loaded = withContext(ioDispatcher) {
            TicketsStorage.loadTickets(workingFolder)
        }
        _tickets.value = loaded
    }

    suspend fun createTicket(title: String? = null, projectId: Int? = null): Persisted<Ticket>? {
        val created = withContext(ioDispatcher) {
            TicketsStorage.createTicket(workingFolder, title, projectId)
        }
        if (created != null) {
            refreshTickets()
        }
        return created
    }

    suspend fun saveTicket(persisted: Persisted<Ticket>, draft: Ticket): Boolean {
        val success = withContext(ioDispatcher) {
            TicketsStorage.saveTicket(draft, persisted.file, draft.details)
        }
        if (success) {
            refreshTickets()
        }
        return success
    }

    suspend fun deleteTicket(persisted: Persisted<Ticket>): Boolean {
        val success = withContext(ioDispatcher) {
            TicketsStorage.deleteTicket(persisted.file)
        }
        if (success) {
            refreshTickets()
        }
        return success
    }
}

