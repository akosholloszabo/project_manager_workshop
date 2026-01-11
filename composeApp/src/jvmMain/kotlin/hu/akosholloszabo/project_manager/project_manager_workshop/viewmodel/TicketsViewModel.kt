package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketsViewModel(
    private val ticketStore: TicketStore,
    private val projectsStorage: ProjectsStorage,
    private val workingFolderStore: WorkingFolderStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _selectedTicketPath = MutableStateFlow<String?>(null)
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    val _editTitle = MutableStateFlow("")
    val _editProjectId = MutableStateFlow(0)
    val _editStatus = MutableStateFlow<TicketStatus?>(null)
    val _editDetails = MutableStateFlow("")
    val editTitle: StateFlow<String> = _editTitle.asStateFlow()
    val editProjectId: StateFlow<Int> = _editProjectId.asStateFlow()
    val editStatus: StateFlow<TicketStatus?> = _editStatus.asStateFlow()
    val editDetails: StateFlow<String> = _editDetails.asStateFlow()

    private val _projects = MutableStateFlow<Map<Int, String>>(emptyMap())

    private val _columns = combine(
        ticketStore.tickets,
        _projects,
        _selectedTicketPath
    ) { tickets, projects, selectedPath ->
        val projectNames = projects
        buildColumns(tickets, projectNames, selectedPath)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    val columns: StateFlow<List<TicketColumnState>> = _columns

    private val _selectedTicket = combine(
        ticketStore.tickets,
        _selectedTicketPath
    ) { tickets, path ->
        path?.let { key -> tickets.firstOrNull { it.file.canonicalPath == key } }
    }.stateIn(scope, SharingStarted.Eagerly, null)
    val selectedTicket: StateFlow<Persisted<Ticket>?> = _selectedTicket

    val projects: StateFlow<Map<Int, String>> = _projects.asStateFlow()

    init {
        scope.launch {
            ticketStore.tickets.collect { tickets ->
                val nextPath = determineSelection(tickets)
                if (nextPath != _selectedTicketPath.value) {
                    _selectedTicketPath.tryEmit(nextPath)
                }
                if (!_isEditing.value) {
                    nextPath?.let { path ->
                        tickets.firstOrNull { it.file.canonicalPath == path }?.value?.let(::emitEditorFields)
                    } ?: resetEditorFields()
                }
            }
        }
        scope.launch { loadProjects() }
    }

    fun refresh() = scope.launch {
        ticketStore.refresh()
    }

    fun createTicket() {
        scope.launch {
            val created = ticketStore.createTicket()
            if (created != null) {
                val path = created.file.canonicalPath
                _selectedTicketPath.tryEmit(path)
                emitEditorFields(created.value)
                _isEditing.tryEmit(true)
            }
        }
    }

    fun selectTicket(entry: Persisted<Ticket>) {
        _selectedTicketPath.tryEmit(entry.file.canonicalPath)
        _isEditing.tryEmit(false)
        emitEditorFields(entry.value)
    }

    fun startEditing() {
        currentSelectedTicket()?.value?.let {
            emitEditorFields(it)
            _isEditing.tryEmit(true)
        }
    }

    fun saveTicket() = scope.launch {
        val current = currentSelectedTicket() ?: return@launch
        val updatedTicket = current.value.copy(
            title = editTitle.value.trim().ifEmpty { current.value.title },
            projectId = editProjectId.value,
            status = editStatus.value,
            details = editDetails.value
        )
        val updatedPersisted = current.copy(value = updatedTicket)
        if (ticketStore.saveTicket(updatedPersisted, updatedTicket)) {
            _isEditing.tryEmit(false)
            emitEditorFields(updatedTicket)
        }
    }

    fun deleteTicket() = scope.launch {
        val current = currentSelectedTicket() ?: return@launch
        if (ticketStore.deleteTicket(current)) {
            clearSelection()
        }
    }

    fun clearSelection() {
        _selectedTicketPath.tryEmit(null)
        resetEditorFields()
        _isEditing.tryEmit(false)
    }

    private fun currentSelectedTicket(): Persisted<Ticket>? {
        val path = _selectedTicketPath.value ?: return null
        return ticketStore.tickets.value.firstOrNull { it.file.canonicalPath == path }
    }

    private suspend fun loadProjects() = withContext(Dispatchers.IO) {
        val loaded = projectsStorage
            .loadProjects(workingFolderStore.session.value)
            .associate { it.value.id to it.value.name }
        _projects.tryEmit(loaded)
    }

    private fun buildColumns(
        tickets: List<Persisted<Ticket>>,
        projectNames: Map<Int, String>,
        selectedPath: String?
    ): List<TicketColumnState> = TicketStatus.entries.map { status ->
        TicketColumnState(
            status = status,
            cards = tickets
                .filter { it.value.status == status }
                .map { persisted ->
                    val projectName = projectNames[persisted.value.projectId] ?: "No project"
                    TicketCardState(
                        persisted = persisted,
                        projectName = projectName,
                        isSelected = persisted.file.canonicalPath == selectedPath
                    )
                }
        )
    }

    private fun determineSelection(tickets: List<Persisted<Ticket>>): String? {
        val current = _selectedTicketPath.value
        return if (current != null && tickets.any { it.file.canonicalPath == current }) {
            current
        } else {
            null
        }
    }

    private fun emitEditorFields(ticket: Ticket) {
        _editTitle.tryEmit(ticket.title)
        _editProjectId.tryEmit(ticket.projectId)
        _editStatus.tryEmit(ticket.status)
        _editDetails.tryEmit(ticket.details)
    }

    private fun resetEditorFields() {
        _editTitle.tryEmit("")
        _editProjectId.tryEmit(0)
        _editStatus.tryEmit(null)
        _editDetails.tryEmit("")
    }
}
