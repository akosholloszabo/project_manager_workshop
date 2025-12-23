package hu.akosholloszabo.project_manager.project_manager_workshop.controller

import hu.akosholloszabo.project_manager.project_manager_workshop.repository.ProjectPayload
import hu.akosholloszabo.project_manager.project_manager_workshop.service.ProjectService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.projectRoutes(service: ProjectService) {
    route("/projects") {
        get {
            call.respond(service.getAll())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val project = service.getById(id)
            if (project == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(project)
        }

        post {
            val payload = call.receive<ProjectPayload>()
            val created = service.create(payload)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val payload = call.receive<ProjectPayload>()
            if (service.update(id, payload)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            if (service.delete(id)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
