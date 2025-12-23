package hu.akosholloszabo.project_manager.project_manager_workshop.controller

import hu.akosholloszabo.project_manager.project_manager_workshop.repository.NotePayload
import hu.akosholloszabo.project_manager.project_manager_workshop.service.NoteService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.noteRoutes(service: NoteService) {
    route("/notes") {
        get {
            call.respond(service.getAll())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val note = service.getById(id)
            if (note == null) {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(note)
        }

        post {
            val payload = call.receive<NotePayload>()
            val created = service.create(payload)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val payload = call.receive<NotePayload>()
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

