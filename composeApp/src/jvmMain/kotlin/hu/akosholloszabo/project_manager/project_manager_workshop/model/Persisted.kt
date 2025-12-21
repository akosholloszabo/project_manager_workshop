package hu.akosholloszabo.project_manager.project_manager_workshop.model

import java.io.File

data class Persisted<T>(val file: File, val value: T)
