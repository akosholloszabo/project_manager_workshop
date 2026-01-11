package hu.akosholloszabo.project_manager.project_manager_workshop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ApiConfig
import hu.akosholloszabo.project_manager.project_manager_workshop.network.NoteServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ProjectServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.TicketServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ProvideStrings
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Strings
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.Texts
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.File
import java.security.KeyStore
import java.util.*
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun main() = application {
    val classLoader = Thread.currentThread().contextClassLoader
    val props = Properties().apply {
        classLoader?.getResourceAsStream("koinServer.properties")?.use { load(it) }
        classLoader?.getResourceAsStream("strings.properties")?.use { load(it) }
    }
    val strings = Strings(props.entries.associate { (k, v) -> k.toString() to v.toString() })
    Texts.init(strings)

    val storageBackend = StorageBackend.SERVER

    val apiConfig = ApiConfig(
        host = props.getProperty("SERVER_HOST", "localhost"),
        httpsPort = props.getProperty("SERVER_HTTPS_PORT", "8443").toInt()
    )
    val trustManager = loadTrustManager(props, classLoader)
    val httpClient = httpClient(trustManager)
    val noteClient = NoteServerClient(apiConfig, httpClient)
    val projectClient = ProjectServerClient(apiConfig, httpClient)
    val ticketClient = TicketServerClient(apiConfig, httpClient)

    val notesStorage = ServerNotesStorage(noteClient, strings)
    val projectsStorage = ServerProjectsStorage(projectClient, strings)
    val ticketsStorage = ServerTicketsStorage(ticketClient, strings)

    val noteStore = NoteStore(null, notesStorage)
    val projectStore = ProjectStore(null, projectsStorage)
    val ticketStore = TicketStore(null, ticketsStorage, storageBackend)

    val workingFolderViewModel = null
    val notesViewModel = NotesViewModel(noteStore)
    val projectsViewModel = ProjectsViewModel(projectStore)
    val ticketsViewModel = TicketsViewModel(ticketStore, projectsStorage, null)

    Window(onCloseRequest = ::exitApplication, title = strings.require("window.title"), state = WindowState()) {
        ProvideStrings(strings) {
            App(
                storageBackend = storageBackend,
                workingFolderViewModel = workingFolderViewModel,
                notesViewModel = notesViewModel,
                projectsViewModel = projectsViewModel,
                ticketsViewModel = ticketsViewModel,
            )
        }
    }
}

private fun loadTrustManager(props: Properties, classLoader: ClassLoader?): X509TrustManager {
    val password = props.getProperty("project_manager.serverKeystorePassword", "changeit").toCharArray()
    val explicitPath = props.getProperty("project_manager.serverKeystorePath")?.takeIf { it.isNotBlank() }
    val keystoreBytes = when {
        explicitPath != null -> File(explicitPath).takeIf(File::exists)?.readBytes()
        else -> classLoader?.getResourceAsStream("ssl/server-keystore.p12")?.use { it.readBytes() }
    } ?: error("Keystore not found; set project_manager.serverKeystorePath or bundle ssl/server-keystore.p12")

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    ByteArrayInputStream(keystoreBytes).use { keyStore.load(it, password) }
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(keyStore)
    return tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
}

private fun httpClient(trustManager: X509TrustManager) = io.ktor.client.HttpClient(io.ktor.client.engine.cio.CIO) {
    engine {
        https { this.trustManager = trustManager }
    }
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}
