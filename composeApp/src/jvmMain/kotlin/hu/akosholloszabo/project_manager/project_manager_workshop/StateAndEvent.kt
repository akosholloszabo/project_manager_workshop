package hu.akosholloszabo.project_manager.project_manager_workshop

public data class StateAndEvent<T>(
    val state: T,
    val event: (T) -> Unit = {}
)
