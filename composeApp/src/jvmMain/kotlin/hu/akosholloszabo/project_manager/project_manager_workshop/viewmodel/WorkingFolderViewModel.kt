package hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel

import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import kotlinx.coroutines.flow.StateFlow

class WorkingFolderViewModel(
    private val workingFolderStore: WorkingFolderStore
) {
    val selectedFolder: StateFlow<String?> = workingFolderStore.workingFolder

    fun confirmFolder(folder: String?) {
        workingFolderStore.updateWorkingFolder(folder)
    }

    fun clearSelection() {
        workingFolderStore.updateWorkingFolder(null)
    }
}
