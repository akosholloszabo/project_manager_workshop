package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

data class StateAndEvent<T>(
    val state: T,
    val event: (T) -> Unit = {}
)
