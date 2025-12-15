package hu.akosholloszabo.project_manager.project_manager_workshop

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
}