package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import hu.akosholloszabo.project_manager.project_manager_workshop.resources.Res
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.cipher_encryption_algorithm
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.cipher_iv_size
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.cipher_key_algorithm
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.cipher_key_speed
import hu.akosholloszabo.project_manager.project_manager_workshop.resources.cipher_message_digest_algorithm
import hu.akosholloszabo.project_manager.project_manager_workshop.utilities.ResourceHelper.getStringResource
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class StorageCipher {
    private val secureRandom: SecureRandom = SecureRandom()
    private val encryptionAlgorithm: String = getStringResource(Res.string.cipher_encryption_algorithm)
    private val messageDigestAlgorithm: String = getStringResource(Res.string.cipher_message_digest_algorithm)
    private val keyAlgorithm: String = getStringResource(Res.string.cipher_key_algorithm)
    private val keySpeed: String = getStringResource(Res.string.cipher_key_speed)
    private val ivSize: Int = getStringResource(Res.string.cipher_iv_size).toInt()

    fun deriveKey(password: String): SecretKey {
        val digest = MessageDigest.getInstance(messageDigestAlgorithm)
        digest.update(keySpeed.toByteArray(StandardCharsets.UTF_8))
        digest.update(password.toByteArray(StandardCharsets.UTF_8))
        return SecretKeySpec(digest.digest(), keyAlgorithm)
    }

    fun encrypt(plainText: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(encryptionAlgorithm)
        val iv = ByteArray(ivSize).also { secureRandom.nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val encoded = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + encoded)
    }

    fun decrypt(cipherText: String, key: SecretKey): String {
        val data = Base64.getDecoder().decode(cipherText)
        val iv = data.copyOfRange(0, ivSize)
        val payload = data.copyOfRange(ivSize, data.size)
        val cipher = Cipher.getInstance(encryptionAlgorithm)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(payload).toString(StandardCharsets.UTF_8)
    }

    fun tryDecrypt(cipherText: String?, key: SecretKey): String? {
        if (cipherText.isNullOrBlank()) return ""
        return runCatching { decrypt(cipherText, key) }.getOrNull()
    }
}
