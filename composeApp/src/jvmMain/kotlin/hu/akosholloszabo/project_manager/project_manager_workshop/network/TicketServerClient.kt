package hu.akosholloszabo.project_manager.project_manager_workshop.network

import hu.akosholloszabo.project_manager.project_manager_workshop.model.Ticket
import hu.akosholloszabo.project_manager.project_manager_workshop.model.TicketPayload
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TicketServerClient(
    private val apiConfig: ApiConfig,
    private val client: HttpClient
) {
    private fun base() = apiConfig.baseUrl.trimEnd('/')

    suspend fun getAll(): List<Ticket> = client.get("${base()}/tickets").body()

    suspend fun create(payload: TicketPayload): Ticket = client.post("${base()}/tickets") {
        contentType(ContentType.Application.Json)
        setBody(payload)
    }.body()

    suspend fun update(id: Int, payload: TicketPayload): Boolean = client.put("${base()}/tickets/$id") {
        contentType(ContentType.Application.Json)
        setBody(payload)
    }.assertNoContent()

    suspend fun delete(id: Int): Boolean = client.delete("${base()}/tickets/$id").assertNoContent()
}

private suspend fun HttpResponse.assertNoContent(): Boolean = status == HttpStatusCode.NoContent
