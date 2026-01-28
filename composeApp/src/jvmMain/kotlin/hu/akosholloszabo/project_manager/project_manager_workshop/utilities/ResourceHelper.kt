package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

object ResourceHelper {
    fun getStringResource(resource: StringResource): String {
        return runBlocking {
            getString(resource)
        }
    }
}
