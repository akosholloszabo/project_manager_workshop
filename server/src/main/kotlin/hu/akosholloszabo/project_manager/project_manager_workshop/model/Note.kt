package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

@Serializable
/**
 * Note model
 */
data class Note(
    val id: Int,
    val title: String,
    val content: String
)
