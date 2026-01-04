package hu.akosholloszabo.project_manager.project_manager_workshop.network

import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val KEYSTORE_PASSWORD = "changeit"

internal object SslSettings {
    fun getKeyStore(): KeyStore {
        val keystoreFile = resolveKeystoreFile()
        return KeyStore.getInstance("PKCS12").apply {
            FileInputStream(keystoreFile).use { load(it, KEYSTORE_PASSWORD.toCharArray()) }
        }
    }

    fun getTrustManagerFactory(): TrustManagerFactory {
        return TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(getKeyStore())
        }
    }

    fun getSslContext(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, getTrustManagerFactory().trustManagers, null)
        return sslContext
    }

    fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory().trustManagers.first { it is X509TrustManager } as X509TrustManager
    }
}

private fun resolveKeystoreFile(): File {
    val candidates = listOfNotNull(
        System.getProperty("project_manager.serverKeystorePath")?.takeUnless(String::isBlank),
        "server/src/main/resources/ssl/server-keystore.p12"
    )
    candidates.forEach { path ->
        resolvePath(path)?.let { return it }
    }
    error("Keystore not found; ensure server/src/main/resources/ssl/server-keystore.p12 exists or set project_manager.serverKeystorePath")
}

private fun resolvePath(path: String): File? {
    val straightforward = File(path).takeIf(File::exists)
    if (straightforward != null) return straightforward
    var current: File? = File(".").absoluteFile
    while (current != null) {
        val alternative = File(current, path)
        if (alternative.exists()) return alternative
        current = current.parentFile
    }
    return null
}
