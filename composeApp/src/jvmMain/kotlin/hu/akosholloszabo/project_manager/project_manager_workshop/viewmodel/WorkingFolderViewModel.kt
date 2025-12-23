package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import kotlinx.coroutines.flow.StateFlow

class WorkingFolderViewModel(
    private val workingFolderStore: WorkingFolderStore
) {
    val selectedFolder: StateFlow<String?> = workingFolderStore.workingFolder
    val session: StateFlow<StorageSession?> = workingFolderStore.session
    val requiresPassword: Boolean get() = workingFolderStore.requiresPassword

    fun confirmFolder(folder: String?, password: String? = null) {
        val trimmed = folder?.trim().takeIf { it?.isNotEmpty() == true } ?: return
        workingFolderStore.confirmFolder(trimmed, password)
    }

    fun clearSelection() {
        workingFolderStore.clearSelection()
    }
}
