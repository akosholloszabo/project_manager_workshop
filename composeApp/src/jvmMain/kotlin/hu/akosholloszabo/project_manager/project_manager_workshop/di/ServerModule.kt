package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.network.ApiConfig
import hu.akosholloszabo.project_manager.project_manager_workshop.network.NoteServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ProjectServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.TicketServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.ByteArrayInputStream
import java.io.File
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

val serverModule = module {
    single {
        ApiConfig(
            host = getProperty("SERVER_HOST"),
            httpsPort = getProperty("SERVER_HTTPS_PORT", "8443").toInt()
        )
    }

    single {
        loadTrustManager(
            serverKeystorePassword = getProperty("project_manager.serverKeystorePassword"),
            serverKeystorePath = getProperty("project_manager.serverKeystorePath")
        )
    }
    singleOf(::httpClient)

    singleOf(::NoteServerClient)
    singleOf(::ProjectServerClient)
    singleOf(::TicketServerClient)

    singleOf(::ServerNotesStorage) bind NotesStorage::class
    singleOf(::ServerProjectsStorage) bind ProjectsStorage::class
    singleOf(::ServerTicketsStorage) bind TicketsStorage::class
}

// Trust store helpers live next to the module for now; move to a shared network module if reused elsewhere.
private fun loadTrustManager(
    //project_manager.serverKeystorePassword
    serverKeystorePassword: String,
    //project_manager.serverKeystorePath
    serverKeystorePath: String,
): X509TrustManager {
    val password = serverKeystorePassword.toCharArray()
    val keystoreBytes = File(serverKeystorePath)
        .takeIf(File::exists)?.readBytes()
        ?: error("Keystore not found; set project_manager.serverKeystorePath or bundle $serverKeystorePath")

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    ByteArrayInputStream(keystoreBytes).use { keyStore.load(it, password) }
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(keyStore)
    return tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
}

private fun httpClient(trustManager: X509TrustManager) = HttpClient(CIO) {
    engine {
        https { this.trustManager = trustManager }
    }
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}
