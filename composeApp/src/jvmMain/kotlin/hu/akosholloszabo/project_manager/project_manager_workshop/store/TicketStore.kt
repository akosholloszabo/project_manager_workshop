package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
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
    private val workingFolderStore: WorkingFolderStore?,
    private val ticketsStorage: TicketsStorage,
    private val storageBackend: StorageBackend
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sessionValue: StorageSession? get() = workingFolderStore?.session?.value

    private val _tickets = MutableStateFlow<List<Persisted<Ticket>>>(emptyList())
    val tickets: StateFlow<List<Persisted<Ticket>>> = _tickets.asStateFlow()

    init {
        scope.launch {
            val sessionFlow = workingFolderStore?.session
            if (sessionFlow == null) {
                refreshTicketsInternal(null)
            } else {
                sessionFlow.collectLatest { session ->
                    refreshTicketsInternal(session)
                }
            }
        }
    }

    // TODO why three refresh functions needed
    fun refresh() {
        refreshTickets()
    }

    fun createTicket(): Persisted<Ticket>? {
        // TODO why new variable
        val session = sessionValue
        if (requiresSession() && session == null) return null
        val created = ticketsStorage.createTicket(
            session, title = "", projectId = -1, status = TicketStatus.Backlog, details = ""
        )
        if (created != null) {
            refreshTickets()
        }
        return created
    }

    fun saveTicket(persisted: Persisted<Ticket>, draft: Ticket): Boolean {
        // TODO why new variable
        val session = sessionValue
        if (requiresSession() && session == null) return false
        val success = ticketsStorage.saveTicket(session, draft, persisted.file, draft.details)
        if (success) {
            refreshTickets()
        }
        return success
    }

    fun deleteTicket(persisted: Persisted<Ticket>): Boolean {
        // TODO why new variable
        val session = sessionValue
        if (requiresSession() && session == null) return false
        val success = ticketsStorage.deleteTicket(session, persisted.file)
        if (success) {
            refreshTickets()
        }
        return success
    }

    private fun refreshTickets() {
        scope.launch {
            refreshTicketsInternal(sessionValue)
        }
    }

    private suspend fun refreshTicketsInternal(session: StorageSession?) {
        val loaded = withContext(Dispatchers.IO) {
            ticketsStorage.loadTickets(session)
        }
        _tickets.emit(loaded)
    }

    private fun requiresSession(): Boolean = storageBackend != StorageBackend.SERVER
}
