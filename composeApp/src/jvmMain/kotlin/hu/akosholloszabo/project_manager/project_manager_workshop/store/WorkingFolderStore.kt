package hu.akosholloszabo.project_manager.project_manager_workshop.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkingFolderStore {
    private val _workingFolder = MutableStateFlow<String?>(null)
    val workingFolder: StateFlow<String?> = _workingFolder.asStateFlow()

    fun updateWorkingFolder(folder: String?) {
        _workingFolder.tryEmit(folder)
    }
}

