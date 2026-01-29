package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TicketsViewModel(
    private val ticketStore: TicketStore,
    private val projectsStorage: ProjectsStorage,
    private val workingFolderStore: WorkingFolderStore?,
) : ViewModel() {
    private val _selectedTicketPath = MutableStateFlow<String?>(null)
    val selectedTicketPath: StateFlow<String?> = _selectedTicketPath.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editTitle = MutableStateFlow("")
    private val _editProjectId = MutableStateFlow(0)
    private val _editStatus = MutableStateFlow<TicketStatus?>(null)
    private val _editDetails = MutableStateFlow("")
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val columns: StateFlow<List<TicketColumnState>> = _columns

    private val _selectedTicket = combine(
        ticketStore.tickets,
        _selectedTicketPath
    ) { tickets, path ->
        path?.let { key -> tickets.firstOrNull { it.file.canonicalPath == key } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val selectedTicket: StateFlow<Persisted<Ticket>?> = _selectedTicket

    val projects: StateFlow<Map<Int, String>> = _projects.asStateFlow()

    init {
        viewModelScope.launch {
            loadProjects()
            ticketStore.tickets.collect { tickets ->
                val nextPath = determineSelection(tickets)
                if (nextPath != selectedTicketPath.value) {
                    _selectedTicketPath.tryEmit(nextPath)
                }
                if (!isEditing.value) {
                    nextPath?.let { path ->
                        tickets.firstOrNull { it.file.canonicalPath == path }?.value?.let(::emitEditorFields)
                    } ?: resetEditorFields()
                }
            }
        }
    }

    fun refresh() = ticketStore.refresh()

    fun createTicket() {
        val created = ticketStore.createTicket()
        if (created != null) {
            val path = created.file.canonicalPath
            _selectedTicketPath.tryEmit(path)
            emitEditorFields(created.value)
            _isEditing.tryEmit(true)
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

    fun saveTicket() {
        val current = currentSelectedTicket() ?: return
        val updatedTicket = current.value.copy(
            title = editTitle.value.trim().ifEmpty { current.value.title },
            projectId = editProjectId.value,
            status = editStatus.value,
            details = editDetails.value
        )
        val updatedPersisted = current.copy(value = updatedTicket)
        val saved = ticketStore.saveTicket(updatedPersisted, updatedTicket)
        if (saved) {
            _isEditing.tryEmit(false)
            emitEditorFields(updatedTicket)
        }
    }

    fun deleteTicket() {
        val current = currentSelectedTicket() ?: return
        val deleted = ticketStore.deleteTicket(current)
        if (deleted) {
            clearSelection()
        }
    }

    fun clearSelection() {
        _selectedTicketPath.tryEmit(null)
        resetEditorFields()
        _isEditing.tryEmit(false)
    }

    fun updateTitle(value: String) = _editTitle.tryEmit(value)
    fun updateProjectId(value: Int) = _editProjectId.tryEmit(value)
    fun updateStatus(value: TicketStatus?) = _editStatus.tryEmit(value)
    fun updateDetails(value: String) = _editDetails.tryEmit(value)

    private fun currentSelectedTicket(): Persisted<Ticket>? =
        selectedTicketPath.value?.let { path ->
            ticketStore.tickets.value.firstOrNull { it.file.canonicalPath == path }
        }

    private fun determineSelection(tickets: List<Persisted<Ticket>>): String? =
        selectedTicketPath.value?.takeIf { current ->
            tickets.any { it.file.canonicalPath == current }
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

    private fun loadProjects() {
        val loaded = projectsStorage
            .loadProjects(workingFolderStore?.session?.value)
            .associate { it.value.id to it.value.name }
        _projects.tryEmit(loaded)
    }
}
