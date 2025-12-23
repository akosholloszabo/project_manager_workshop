package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object StorageCipher {
    private const val algorithm = "AES/GCM/NoPadding"
    private const val keySeed = "project-manager workshop 2025"
    private const val ivSize = 12
    private val secureRandom = SecureRandom()

    fun deriveKey(password: String): SecretKey {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(keySeed.toByteArray(StandardCharsets.UTF_8))
        digest.update(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest.digest(), "AES")
    }

    fun encrypt(plainText: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(algorithm)
        val iv = ByteArray(ivSize).also { secureRandom.nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encoded = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + encoded)
    }

    fun decrypt(cipherText: String, key: SecretKey): String {
        val data = Base64.getDecoder().decode(cipherText)
        val iv = data.copyOfRange(0, ivSize)
        val payload = data.copyOfRange(ivSize, data.size)
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(payload).toString(StandardCharsets.UTF_8)
    }

    fun tryDecrypt(cipherText: String?, key: SecretKey): String? {
        if (cipherText.isNullOrBlank()) return ""
        return runCatching { decrypt(cipherText, key) }.getOrNull()
    }
}

