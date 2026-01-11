package hu.akosholloszabo.project_manager.project_manager_workshop.model

data class StorageSpec(
    val folderName: String,
    val primaryExtension: String,
    val fallbackName: String,
    val detailExtension: String? = null
)
