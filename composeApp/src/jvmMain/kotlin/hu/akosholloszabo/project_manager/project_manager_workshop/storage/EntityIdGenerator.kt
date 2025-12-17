package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import java.util.*

object EntityIdGenerator {
    fun newId(): Int = UUID.randomUUID().hashCode()
}

