package hu.akosholloszabo.project_manager.project_manager_workshop

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ApiConfig
import hu.akosholloszabo.project_manager.project_manager_workshop.network.NoteServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ProjectServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.TicketServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.EncryptedTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.NotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.PlainTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerNotesStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerProjectsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.ServerTicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.storage.TicketsStorage
import hu.akosholloszabo.project_manager.project_manager_workshop.store.EncryptedWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.NoteStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.PlainWorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.ProjectStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.TicketStore
import hu.akosholloszabo.project_manager.project_manager_workshop.store.WorkingFolderStore
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.NotesViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.ProjectsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.TicketsViewModel
import hu.akosholloszabo.project_manager.project_manager_workshop.viewmodel.WorkingFolderViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.ByteArrayInputStream
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

val appModule = module {
    single { StorageBackend.fromPropertyValue(getProperty("storage.backend")) }
    single<X509TrustManager> {
        val loader = Thread.currentThread().contextClassLoader
        val keyStoreBytes: ByteArray = loader.getResourceAsStream("ssl/server-keystore.p12")?.use { it.readBytes() }
            ?: error("Keystore not found; ensure ssl/server-keystore.p12 is bundled or configure project_manager.serverKeystorePath")
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        ByteArrayInputStream(keyStoreBytes).use {
            keyStore.load(
                it,
                getProperty<String>("project_manager.serverKeystorePassword").toCharArray()
            )
        }
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(keyStore)
        trustManagerFactory.trustManagers.filterIsInstance<X509TrustManager>().first()
    }
    single {
        ApiConfig(
            host = getProperty("SERVER_HOST"),
            httpsPort = getProperty<String>("SERVER_HTTPS_PORT").toInt()
        )
    }
    single {
        HttpClient(CIO) {
            engine {
                https {
                    trustManager = get<X509TrustManager>()
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    singleOf(::NoteServerClient)
    singleOf(::ProjectServerClient)
    singleOf(::TicketServerClient)
    factory<NotesStorage> {
        when (get<StorageBackend>()) {
            StorageBackend.SERVER -> ServerNotesStorage(get())
            StorageBackend.ENCRYPTED -> EncryptedNotesStorage()
            StorageBackend.LOCAL -> PlainNotesStorage()
        }
    }
    factory<ProjectsStorage> {
        when (get<StorageBackend>()) {
            StorageBackend.SERVER -> ServerProjectsStorage(get())
            StorageBackend.ENCRYPTED -> EncryptedProjectsStorage()
            StorageBackend.LOCAL -> PlainProjectsStorage()
        }
    }
    factory<TicketsStorage> {
        when (get<StorageBackend>()) {
            StorageBackend.SERVER -> ServerTicketsStorage(get())
            StorageBackend.ENCRYPTED -> EncryptedTicketsStorage()
            StorageBackend.LOCAL -> PlainTicketsStorage()
        }
    }

    single<WorkingFolderStore> {
        when (get<StorageBackend>()) {
            StorageBackend.ENCRYPTED -> EncryptedWorkingFolderStore()
            else -> PlainWorkingFolderStore()
        }
    }

    singleOf(::TicketStore)
    singleOf(::ProjectStore)
    singleOf(::NoteStore)

    singleOf(::WorkingFolderViewModel)
    singleOf(::TicketsViewModel)
    singleOf(::ProjectsViewModel)
    singleOf(::NotesViewModel)
}
