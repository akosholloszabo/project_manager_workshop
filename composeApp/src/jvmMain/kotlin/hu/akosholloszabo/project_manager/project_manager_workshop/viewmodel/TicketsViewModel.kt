package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Persisted
import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketCardState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketColumnState
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketStatus
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketsScreenState
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TicketsViewModel(
    private val ticketStore: TicketStore,
    private val workingFolder: String?,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = coroutineScope

    private val _selectedTicketPath = MutableStateFlow<String?>(null)
    private val _editorTicket = MutableStateFlow<Ticket?>(null)
    private val _isEditing = MutableStateFlow(false)
    private val _projects = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    private val _boardVersion = MutableStateFlow(0)

    private val _baseUiState = combine(
        ticketStore.tickets,
        _projects,
        _selectedTicketPath,
        _editorTicket,
        _isEditing
    ) { tickets: List<Persisted<Ticket>>,
        projects: List<Pair<Int, String>>,
        selectedPath: String?,
        editor: Ticket?,
        editing: Boolean ->
        CoreUiState(
            tickets = tickets,
            projects = projects,
            selectedPath = selectedPath,
            editor = editor,
            editing = editing
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

        TicketsScreenState(
            columns = columns,
            selectedTicket = selectedTicket,
            editorTicket = if (coreState.editing) coreState.editor else selectedTicket?.value,
            isEditing = coreState.editing,
            projects = coreState.projects,
            boardVersion = version
        )
    }.stateIn(scope, SharingStarted.Eagerly, TicketsScreenState())

    init {
        scope.launch {
            ticketStore.tickets.collect { tickets ->
                val nextPath = determineSelection(tickets)
                if (_selectedTicketPath.value != nextPath) {
                    _selectedTicketPath.value = nextPath
                }
                if (!_isEditing.value) {
                    val editorValue = nextPath?.let { path ->
                        tickets.firstOrNull { it.file.canonicalPath == path }?.value
                    }
                    _editorTicket.value = editorValue
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
                _selectedTicketPath.value = path
                _editorTicket.value = created.value
                _isEditing.value = true
                bumpBoardVersion()
            }
        }
    }

    fun selectTicket(entry: Persisted<Ticket>) {
        _selectedTicketPath.value = entry.file.canonicalPath
        if (!_isEditing.value) {
            _editorTicket.value = entry.value
        }
    }

    fun startEditing() {
        val current = currentSelectedTicket()?.value ?: return
        _editorTicket.value = current
        _isEditing.value = true
    }

    fun updateEditorTicket(updated: Ticket) {
        _editorTicket.value = updated
    }

    fun saveTicket() {
        scope.launch {
            val current = currentSelectedTicket() ?: return@launch
            val draft = _editorTicket.value ?: current.value
            val updated = current.copy(value = draft)
            if (ticketStore.saveTicket(updated, draft)) {
                _isEditing.value = false
                _editorTicket.value = draft
                bumpBoardVersion()
            }
        }
    }

    fun deleteTicket() {
        scope.launch {
            val current = currentSelectedTicket() ?: return@launch
            if (ticketStore.deleteTicket(current)) {
                _selectedTicketPath.value = null
                _editorTicket.value = null
                _isEditing.value = false
                bumpBoardVersion()
            }
        }
    }

    fun clearSelection() {
        _selectedTicketPath.value = null
        _editorTicket.value = null
        _isEditing.value = false
    }

    private fun currentSelectedTicket(): Persisted<Ticket>? {
        val path = _selectedTicketPath.value ?: return null
        return ticketStore.tickets.value.firstOrNull { it.file.canonicalPath == path }
    }

    private suspend fun loadProjects() {
        val loaded = withContext(ioDispatcher) {
            ProjectsStorage.loadProjects(workingFolder)
                .map { it.value.id to it.value.name }
        }
        _projects.value = loaded
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
        _boardVersion.value += 1
    }

    private data class CoreUiState(
        val tickets: List<Persisted<Ticket>>,
        val projects: List<Pair<Int, String>>,
        val selectedPath: String?,
        val editor: Ticket?,
        val editing: Boolean
    )
}
