package hu.akosholloszabo.project_manager.project_manager_workshop.di

import hu.akosholloszabo.project_manager.project_manager_workshop.model.StorageBackend
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ApiConfig
import hu.akosholloszabo.project_manager.project_manager_workshop.network.NoteServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.ProjectServerClient
import hu.akosholloszabo.project_manager.project_manager_workshop.network.TicketServerClient
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


val serverModule = module {
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
}
