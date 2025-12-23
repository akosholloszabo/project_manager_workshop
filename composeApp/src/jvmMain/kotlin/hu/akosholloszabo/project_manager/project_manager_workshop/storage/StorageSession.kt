package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import javax.crypto.SecretKey

/**
 * Represents the selected working folder and optional encryption key for accesses.
 */
data class StorageSession(
    val folderPath: String,
    val encryptionKey: SecretKey?
)
