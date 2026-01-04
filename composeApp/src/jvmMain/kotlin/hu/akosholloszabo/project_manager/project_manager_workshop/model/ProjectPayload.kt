package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable

/**
 * Data transfer object matching the server project routes.
 *
 * */
@Serializable
data class ProjectPayload(
    val name: String,
    val description: String,
    val details: String,
)
