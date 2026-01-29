package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

import java.util.*

fun loadProperties(resourcePath: String): Properties {
    val props = Properties()
    val normalized = resourcePath.removePrefix("/")
    val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(normalized)
    stream?.use { props.load(it) }
    return props
}
