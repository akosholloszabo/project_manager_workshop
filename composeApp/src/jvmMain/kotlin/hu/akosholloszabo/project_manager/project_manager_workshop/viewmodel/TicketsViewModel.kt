package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketsScreenState
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketsViewModel(
    private val ticketStore: TicketStore,
    private val workingFolder: String?,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val scope = coroutineScope

    private val _selectedTicketPath = MutableStateFlow<String?>(null)
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editTitle = MutableStateFlow("")
    private val _editProjectId = MutableStateFlow(0)
    private val _editStatus = MutableStateFlow(TicketStatus.default)
    private val _editDetails = MutableStateFlow("")
    val editTitle: StateFlow<String> = _editTitle.asStateFlow()
    val editProjectId: StateFlow<Int> = _editProjectId.asStateFlow()
    val editStatus: StateFlow<TicketStatus> = _editStatus.asStateFlow()
    val editDetails: StateFlow<String> = _editDetails.asStateFlow()

    private val _projects = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    private val _boardVersion = MutableStateFlow(0)

    private data class BaseAccumulator(
        val tickets: List<Persisted<Ticket>>,
        val projects: List<Pair<Int, String>>,
        val selectedPath: String? = null,
        val editing: Boolean = false,
        val title: String = "",
        val projectId: Int = 0,
        val status: TicketStatus = TicketStatus.default,
        val details: String = ""
    )

    private val _baseUiState = ticketStore.tickets
        .combine(_projects) { tickets, projects ->
            BaseAccumulator(tickets = tickets, projects = projects)
        }
        .combine(_selectedTicketPath) { acc, selectedPath ->
            acc.copy(selectedPath = selectedPath)
        }
        .combine(_isEditing) { acc, editing ->
            acc.copy(editing = editing)
        }
        .combine(_editTitle) { acc, title ->
            acc.copy(title = title)
        }
        .combine(_editProjectId) { acc, projectId ->
            acc.copy(projectId = projectId)
        }
        .combine(_editStatus) { acc, status ->
            acc.copy(status = status)
        }
        .combine(_editDetails) { acc, details ->
            acc.copy(details = details)
        }
        .map { acc ->
            CoreUiState(
                tickets = acc.tickets,
                projects = acc.projects,
                selectedPath = acc.selectedPath,
                editing = acc.editing,
                editorFields = Ticket(
                    id = acc.tickets.firstOrNull { it.file.canonicalPath == acc.selectedPath }?.value?.id ?: 0,
                    title = acc.title,
                    projectId = acc.projectId,
                    status = acc.status,
                    details = acc.details
                )
            )
        }

    val uiState: StateFlow<TicketsScreenState> = combine(
        _baseUiState,
        _boardVersion
    ) { coreState, version ->
        val selectedTicket = coreState.selectedPath?.let { path ->
            coreState.tickets.firstOrNull { it.file.canonicalPath == path }
        }
        val projectNames = coreState.projects.associate { it.first to it.second }
        val columns = buildColumns(coreState.tickets, projectNames, coreState.selectedPath)

        val editorTicket = if (coreState.editing) {
            selectedTicket?.value?.copy(
                title = coreState.editorFields.title,
                projectId = coreState.editorFields.projectId,
                status = coreState.editorFields.status,
                details = coreState.editorFields.details
            ) ?: coreState.editorFields
        } else {
            selectedTicket?.value
        }

        TicketsScreenState(
            columns = columns,
            selectedTicket = selectedTicket,
            editorTicket = editorTicket,
            isEditing = coreState.editing,
            projects = coreState.projects,
            boardVersion = version
        )
    }.stateIn(scope, SharingStarted.Eagerly, TicketsScreenState())

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

    fun refresh() {
        scope.launch {
            ticketStore.refresh()
            bumpBoardVersion()
        }
    }

    fun createTicket() {
        scope.launch {
            val created = ticketStore.createTicket()
            if (created != null) {
                val path = created.file.canonicalPath
                _selectedTicketPath.tryEmit(path)
                emitEditorFields(created.value)
                _isEditing.tryEmit(true)
                bumpBoardVersion()
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

    fun updateTitle(value: String) {
        _editTitle.tryEmit(value)
    }

    fun updateProjectId(value: Int) {
        _editProjectId.tryEmit(value)
    }

    fun updateStatus(value: TicketStatus) {
        _editStatus.tryEmit(value)
    }

    fun updateDetails(value: String) {
        _editDetails.tryEmit(value)
    }

    fun saveTicket() {
        scope.launch {
            val current = currentSelectedTicket() ?: return@launch
            val updatedTicket = current.value.copy(
                title = _editTitle.value.trim().ifEmpty { current.value.title },
                projectId = _editProjectId.value,
                status = _editStatus.value,
                details = _editDetails.value
            )
            val updatedPersisted = current.copy(value = updatedTicket)
            if (ticketStore.saveTicket(updatedPersisted, updatedTicket)) {
                _isEditing.tryEmit(false)
                emitEditorFields(updatedTicket)
                bumpBoardVersion()
            }
        }
    }

    fun deleteTicket() {
        scope.launch {
            val current = currentSelectedTicket() ?: return@launch
            if (ticketStore.deleteTicket(current)) {
                clearSelection()
                bumpBoardVersion()
            }
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

    private suspend fun loadProjects() {
        val loaded = withContext(Dispatchers.IO) {
            ProjectsStorage.loadProjects(workingFolder).map { it.value.id to it.value.name }
        }
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

    private fun bumpBoardVersion() {
        _boardVersion.tryEmit(_boardVersion.value + 1)
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
        _editStatus.tryEmit(TicketStatus.default)
        _editDetails.tryEmit("")
    }

    private data class CoreUiState(
        val tickets: List<Persisted<Ticket>>,
        val projects: List<Pair<Int, String>>,
        val selectedPath: String?,
        val editing: Boolean,
        val editorFields: Ticket
    )
}
