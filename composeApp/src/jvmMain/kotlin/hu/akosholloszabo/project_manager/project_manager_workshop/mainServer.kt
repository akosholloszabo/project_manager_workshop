package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ApiConfig
import hu.akosholloszabo.project_manager.project_manager_workshop.network.NoteServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ProjectServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.TicketServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.window_title
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.loadProperties
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.File
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun main() = application { 
    val props = loadProperties("/koinServer.properties")
    val backend = StorageBackend.fromPropertyValue(props.getProperty("storage.backend"))
    val apiConfig = ApiConfig(
        host = props.getProperty("SERVER_HOST", "localhost"),
        httpsPort = props.getProperty("SERVER_HTTPS_PORT", "8443").toInt()
    )
    val trustManager = loadTrustManager(
        serverKeystorePassword = props.getProperty("project_manager.serverKeystorePassword"),
        serverKeystorePath = props.getProperty("project_manager.serverKeystorePath")
    )
    val httpClient = httpClient(trustManager)
    val noteServerClient = NoteServerClient(apiConfig, httpClient)
    val projectServerClient = ProjectServerClient(apiConfig, httpClient)
    val ticketServerClient = TicketServerClient(apiConfig, httpClient)
    val notesStorage = ServerNotesStorage(noteServerClient)
    val projectsStorage = ServerProjectsStorage(projectServerClient)
    val ticketsStorage = ServerTicketsStorage(ticketServerClient)
    val noteStore = NoteStore(null, notesStorage)
    val projectStore = ProjectStore(null, projectsStorage)
    val ticketStore = TicketStore(null, ticketsStorage, backend)
    val notesViewModel = NotesViewModel(noteStore)
    val projectsViewModel = ProjectsViewModel(projectStore)
    val ticketsViewModel = TicketsViewModel(ticketStore, projectsStorage, null)

    Window(
        onCloseRequest = ::exitApplication,
        title = getStringResource(Res.string.window_title),
        state = WindowState()
    ) {
        App(
            storageBackend = backend,
            notesViewModel = notesViewModel,
            projectsViewModel = projectsViewModel,
            ticketsViewModel = ticketsViewModel,
            workingFolderViewModel = null,
        )
    }
}

// Trust store helpers adapted from the previous Koin module.
fun loadTrustManager(
    serverKeystorePassword: String?,
    serverKeystorePath: String?,
): X509TrustManager {
    require(!serverKeystorePassword.isNullOrBlank()) { "project_manager.serverKeystorePassword must be set" }
    require(!serverKeystorePath.isNullOrBlank()) { "project_manager.serverKeystorePath must be set" }

    val password = serverKeystorePassword.toCharArray()
    val keystoreBytes = File(serverKeystorePath)
        .takeIf(File::exists)?.readBytes()
        ?: error("Keystore not found; set project_manager.serverKeystorePath or bundle $serverKeystorePath")

    val keyStore = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType())
    ByteArrayInputStream(keystoreBytes).use { keyStore.load(it, password) }
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(keyStore)
    return tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
}

fun httpClient(trustManager: X509TrustManager): HttpClient = HttpClient(CIO) {
    engine {
        https { this.trustManager = trustManager }
    }
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}
