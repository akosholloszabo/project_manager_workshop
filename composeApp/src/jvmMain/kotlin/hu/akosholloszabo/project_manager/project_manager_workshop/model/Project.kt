package hu.akosholloszabo.project_manager.project_manager_workshop.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
/**
 * Project model
 */
data class Project(
    val id: Int,
    val name: String,
    val description: String,
    @Transient
    val details: String = ""
)
