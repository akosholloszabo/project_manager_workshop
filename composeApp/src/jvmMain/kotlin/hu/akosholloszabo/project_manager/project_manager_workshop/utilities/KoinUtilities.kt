package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

import org.koin.core.Koin

object KoinUtilities {
    fun Koin.getTextOrException(key: String): String = getProperty(key) ?: throw Exception("Missing Text")
}
