package hu.akosholloszabo.project_manager.project_manager_workshop

sealed class Screen {
    object Notes : Screen()
    object Projects : Screen()
    object Tickets : Screen()
}

