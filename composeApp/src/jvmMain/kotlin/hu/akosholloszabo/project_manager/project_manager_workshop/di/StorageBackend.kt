package hu.akosholloszabo.project_manager.project_manager_workshop.di

/**
 * Determines whether the client should use local storage or the server API.
 */
enum class StorageBackend {
    LOCAL,
    ENCRYPTED,
    SERVER;

    companion object {
        private val lookup = values().associateBy { it.name }

        fun fromPropertyValue(value: String?): StorageBackend =
            value
                ?.takeUnless(String::isNullOrBlank)
                ?.uppercase()
                ?.let { lookup[it] }
                ?: LOCAL
    }
}
