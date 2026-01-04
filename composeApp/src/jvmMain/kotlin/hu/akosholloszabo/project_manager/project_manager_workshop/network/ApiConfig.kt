package hu.akosholloszabo.project_manager.project_manager_workshop.network

/**
 * Provides base addresses for the server APIs.
 */
data class ApiConfig(val host: String, val httpsPort: Int) {
    val baseUrl get() = "https://$host:$httpsPort"
}
