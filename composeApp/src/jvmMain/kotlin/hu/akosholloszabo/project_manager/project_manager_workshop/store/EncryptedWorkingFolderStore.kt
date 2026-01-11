package hu.akosholloszabo.project_manager.project_manager_workshop.store

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageSession
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.StorageCipher

class EncryptedWorkingFolderStore(val storageCipher: StorageCipher) : WorkingFolderStore() {
    override val requiresPassword: Boolean = true

    override fun confirmFolder(folder: String, password: String?) {
        val secret = password?.trim().takeUnless { it.isNullOrEmpty() } ?: return
        val key = storageCipher.deriveKey(secret)
        updateSession(StorageSession(folder, key))
    }
}
