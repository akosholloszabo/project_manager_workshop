package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

@Serializable
data class NotePayload(
    val title: String,
    val content: String
)
