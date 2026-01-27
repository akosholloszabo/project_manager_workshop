package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class WorkingFolderStore {
    //TODO this should be private
    protected val _session = MutableStateFlow<StorageSession?>(null)
    val session: StateFlow<StorageSession?> = _session.asStateFlow()

    protected val _workingFolder = MutableStateFlow<String?>(null)
    val workingFolder: StateFlow<String?> = _workingFolder.asStateFlow()

    protected fun updateSession(session: StorageSession?) {
        _session.tryEmit(session)
        _workingFolder.tryEmit(session?.folderPath)
    }

    fun clearSelection() {
        updateSession(null)
    }

    abstract val requiresPassword: Boolean

    abstract fun confirmFolder(folder: String, password: String? = null)
}
