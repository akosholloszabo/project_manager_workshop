package hu.akosholloszabo.project_manager.project_manager_workshop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform