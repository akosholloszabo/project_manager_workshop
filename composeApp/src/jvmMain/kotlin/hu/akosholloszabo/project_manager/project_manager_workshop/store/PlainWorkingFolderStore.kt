package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession

class PlainWorkingFolderStore : WorkingFolderStore() {
    override val requiresPassword: Boolean = false

    override fun confirmFolder(folder: String, password: String?) {
        updateSession(StorageSession(folder, null))
    }
}
