package hu.akosholloszabo.project_manager.project_manager_workshop.network

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Project
import hu.akosholloszabo.project_manager.project_manager_workshop.model.ProjectPayload
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ProjectServerClient(
    private val apiConfig: ApiConfig,
    private val client: HttpClient
) {
    private fun base() = apiConfig.baseUrl.trimEnd('/')

    suspend fun getAll(): List<Project> = client.get("${base()}/projects").body()

    suspend fun create(payload: ProjectPayload): Project = client.post("${base()}/projects") {
        contentType(ContentType.Application.Json)
        setBody(payload)
    }.body()

    suspend fun update(id: Int, payload: ProjectPayload): Boolean = client.put("${base()}/projects/$id") {
        contentType(ContentType.Application.Json)
        setBody(payload)
    }.assertNoContent()

    suspend fun delete(id: Int): Boolean = client.delete("${base()}/projects/$id").assertNoContent()
}

private suspend fun HttpResponse.assertNoContent(): Boolean = status == HttpStatusCode.NoContent
