package hu.akosholloszabo.project_manager.project_manager_workshop.network

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Note
import hu.akosholloszabo.project_manager.project_manager_workshop.model.NotePayload
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class NoteServerClient(
    private val apiConfig: ApiConfig,
    private val client: HttpClient
) {
    private fun base() = apiConfig.baseUrl.trimEnd('/')

    suspend fun getAll(): List<Note> = client.get("${base()}/notes").body()

    suspend fun create(payload: NotePayload): Note = client.post("${base()}/notes") {
        contentType(ContentType.Application.Json)
        setBody(payload)
    }.body()

    suspend fun update(id: Int, payload: NotePayload): Boolean = client.put("${base()}/notes/$id") {
        contentType(ContentType.Application.Json)
        setBody(payload)
    }.assertNoContent()

    suspend fun delete(id: Int): Boolean = client.delete("${base()}/notes/$id").assertNoContent()
}

private suspend fun HttpResponse.assertNoContent(): Boolean = status == HttpStatusCode.NoContent
