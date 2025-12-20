package hu.akosholloszabo.project_manager.project_manager_workshop.actions

sealed interface CrudAction {
    data object Create : CrudAction
    data object Edit : CrudAction
    data object Save : CrudAction
    data object Delete : CrudAction
}

