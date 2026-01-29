package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TicketStore(
    private val workingFolderStore: WorkingFolderStore?,
    private val ticketsStorage: TicketsStorage,
    private val storageBackend: StorageBackend
) {

    private val _tickets = MutableStateFlow<List<Persisted<Ticket>>>(emptyList())
    val tickets: StateFlow<List<Persisted<Ticket>>> = _tickets.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            workingFolderStore?.session?.let { sessionFlow ->
                sessionFlow.collectLatest { session ->
                    refresh(session)
                }
            } ?: refresh(null)
        }
    }

    fun refresh(session: StorageSession? = workingFolderStore?.session?.value) =
        _tickets.tryEmit(ticketsStorage.loadTickets(session))

    fun createTicket(): Persisted<Ticket>? =
        ticketsStorage.createTicket(
            workingFolderStore?.session?.value,
            title = "",
            projectId = -1,
            status = TicketStatus.BACKLOG,
            details = ""
        )?.also {
            refresh()
        }

    fun saveTicket(persisted: Persisted<Ticket>, draft: Ticket): Boolean =
        ticketsStorage.saveTicket(
            workingFolderStore?.session?.value,
            draft,
            persisted.file,
            draft.details
        )
            .also { success ->
                if (success) {
                    refresh()
                }
            }

    fun deleteTicket(persisted: Persisted<Ticket>): Boolean =
        ticketsStorage.deleteTicket(
            workingFolderStore?.session?.value,
            persisted.file
        )
            .also { success ->
                if (success) {
                    refresh()
                }
            }
}
