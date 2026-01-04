package hu.akosholloszabo.project_manager.project_manager_workshop.network

import hu.akosholloszabo.project_manager.project_manager_workshop.SERVER_HOST
import hu.akosholloszabo.project_manager.project_manager_workshop.SERVER_HTTPS_PORT

/**
 * Provides base addresses for the server APIs.
 */
data class ApiConfig(val baseUrl: String = defaultBaseUrl()) {
    companion object {
        private fun defaultBaseUrl(): String {
            val override = System.getProperty("project_manager.serverBaseUrl")
                ?.takeUnless { it.isBlank() }
            return override ?: "https://$SERVER_HOST:$SERVER_HTTPS_PORT"
        }
    }
}
