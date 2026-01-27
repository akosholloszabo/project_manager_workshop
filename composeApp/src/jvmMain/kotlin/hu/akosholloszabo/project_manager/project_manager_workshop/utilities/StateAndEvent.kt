package hu.akosholloszabo.project_manager.project_manager_workshop.utilities

data class StateAndEvent<T>(
    // TODO I would use value
    val state: T,
    val event: (T) -> Unit = {}
)
