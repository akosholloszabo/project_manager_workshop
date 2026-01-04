package hu.akosholloszabo.project_manager.project_manager_workshop

import com.typesafe.config.ConfigFactory
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.noteRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.projectRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.controller.ticketRoutes
import hu.akosholloszabo.project_manager.project_manager_workshop.db.DatabaseFactory
import hu.akosholloszabo.project_manager.project_manager_workshop.di.serverModule
import io.ktor.network.tls.certificates.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import java.io.File
import java.security.KeyStore

fun main() {
    val runtimeConfig = HoconApplicationConfig(ConfigFactory.load())
    embeddedServer(Netty, applicationEnvironment {
        config = runtimeConfig
    }, {
        envConfig(runtimeConfig)
    }, module = Application::module).start(wait = true)
}

private fun ApplicationEngine.Configuration.envConfig(config: ApplicationConfig) {
    val deployment = config.config("ktor.deployment")
    val host = deployment.property("host").getString()

    val sslSettings = loadSslSettings(config)
    val sslPort = deployment.propertyOrNull("sslPort")?.getString()?.toInt() ?: 8443
    sslConnector(
        keyStore = sslSettings.keyStore,
        keyAlias = sslSettings.keyAlias,
        keyStorePassword = { sslSettings.keyStorePassword },
        privateKeyPassword = { sslSettings.privateKeyPassword }) {
        this.host = host
        this.port = sslPort
    }
}


fun Application.module(config: ApplicationConfig = environment.config) {
    install(ContentNegotiation) {
        json()
    }
    install(CallLogging)
    DatabaseFactory.init(config)
    install(Koin) {
        modules(serverModule)
    }
    routing {
        projectRoutes(get())
        noteRoutes(get())
        ticketRoutes(get())
    }
}

private data class SslSettings(
    val keyStore: KeyStore,
    val keyAlias: String,
    val keyStorePassword: CharArray,
    val privateKeyPassword: CharArray
)

private fun loadSslSettings(config: ApplicationConfig): SslSettings {
    val sslConfig = runCatching { config.config("server.ssl") }.getOrNull()
        ?: config.config("ktor.security.ssl")
    val keyStorePath = sslConfig.property("keyStore").getString()
    val keyAlias = sslConfig.property("keyAlias").getString()
    val keyStorePassword = sslConfig.property("keyStorePassword").getString()
    val privateKeyPassword = sslConfig.propertyOrNull("privateKeyPassword")?.getString()
        ?: keyStorePassword
    val keyStoreStream =
        Thread.currentThread().contextClassLoader.getResourceAsStream(keyStorePath)
            ?: File(keyStorePath).takeIf(File::exists)?.inputStream()
            ?: error("Keystore '$keyStorePath' not found on classpath or filesystem")
    val keyStore = KeyStore.getInstance("PKCS12").apply {
        keyStoreStream.use { load(it, keyStorePassword.toCharArray()) }
    }
    return SslSettings(
        keyStore = keyStore,
        keyAlias = keyAlias,
        keyStorePassword = keyStorePassword.toCharArray(),
        privateKeyPassword = privateKeyPassword.toCharArray()
    )
}
