package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageCipher
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface WorkingFolderStore {
    val workingFolder: StateFlow<String?>
    val session: StateFlow<StorageSession?>
    val requiresPassword: Boolean

    fun confirmFolder(folder: String, password: String? = null)
    fun clearSelection()
}

abstract class BaseWorkingFolderStore : WorkingFolderStore {
    protected val _session = MutableStateFlow<StorageSession?>(null)
    override val session: StateFlow<StorageSession?> = _session.asStateFlow()

    protected val _workingFolder = MutableStateFlow<String?>(null)
    override val workingFolder: StateFlow<String?> = _workingFolder.asStateFlow()

    protected fun updateSession(session: StorageSession?) {
        _session.tryEmit(session)
        _workingFolder.tryEmit(session?.folderPath)
    }

    override fun clearSelection() {
        updateSession(null)
    }
}

class PlainWorkingFolderStore : BaseWorkingFolderStore() {
    override val requiresPassword: Boolean = false

    override fun confirmFolder(folder: String, password: String?) {
        updateSession(StorageSession(folder, null))
    }
}

class EncryptedWorkingFolderStore : BaseWorkingFolderStore() {
    override val requiresPassword: Boolean = true

    override fun confirmFolder(folder: String, password: String?) {
        val secret = password?.trim().takeUnless { it.isNullOrEmpty() } ?: return
        val key = StorageCipher.deriveKey(secret)
        updateSession(StorageSession(folder, key))
    }
}
