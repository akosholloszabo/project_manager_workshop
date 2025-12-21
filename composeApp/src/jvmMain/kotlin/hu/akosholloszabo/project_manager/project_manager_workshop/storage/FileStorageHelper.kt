package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object FileStorageHelper {
    private const val TIMESTAMP_PATTERN = "yyyyMMdd-HHmmss"
    private val timestampFormatter = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN, Locale.US)
    val defaultJson = Json { encodeDefaults = true; prettyPrint = true }

    data class StorageSpec(
        val folderName: String,
        val primaryExtension: String,
        val fallbackName: String,
        val detailExtension: String? = null
    )

    fun ensureDirectory(root: String?, folderName: String): File? {
        val folder = root?.let { File(it, folderName) } ?: return null
        if (!folder.exists() && !folder.mkdirs()) return null
        return folder
    }

    fun listFiles(folder: File, extension: String): List<File> {
        val normalizedExtension = normalizeExtension(extension)
        return folder.listFiles { file ->
            file.isFile && file.extension.equals(normalizedExtension, ignoreCase = true)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun createTimestampedFile(folder: File, rawName: String, fallback: String, extension: String): File {
        val sanitized = sanitizeFileName(rawName, fallback)
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val normalizedExtension = ensureDotPrefix(extension)
        return File(folder, "$sanitized-$timestamp$normalizedExtension")
    }

    fun sanitizeFileName(raw: String, fallback: String): String {
        val sanitized = raw.trim().ifEmpty { fallback }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .replace(Regex("\\s+"), "-")
        return sanitized.trim('-').ifEmpty { fallback }
    }

    fun getSidecarFile(primaryFile: File, extension: String): File {
        val parent = primaryFile.parentFile ?: primaryFile
        val normalizedExtension = ensureDotPrefix(extension)
        return File(parent, primaryFile.nameWithoutExtension + normalizedExtension)
    }

    fun ensureSidecarFile(primaryFile: File, extension: String): File {
        val sidecar = getSidecarFile(primaryFile, extension)
        if (!sidecar.exists()) {
            sidecar.writeText("")
        }
        return sidecar
    }

    fun readSidecar(primaryFile: File, extension: String): String {
        return runCatching {
            ensureSidecarFile(primaryFile, extension).readText()
        }.getOrDefault("")
    }

    fun writeSidecar(primaryFile: File, extension: String, content: String) {
        ensureSidecarFile(primaryFile, extension).writeText(content)
    }

    fun deleteSidecar(primaryFile: File, extension: String) {
        getSidecarFile(primaryFile, extension).delete()
    }

    private fun normalizeExtension(extension: String): String {
        return extension.trimStart('.')
    }

    private fun ensureDotPrefix(extension: String): String {
        val normalized = normalizeExtension(extension)
        return ".$normalized"
    }

    fun ensureStorageDirectory(root: String?, spec: StorageSpec): File? {
        return ensureDirectory(root, spec.folderName)
    }

    fun listStorageFiles(folder: File, spec: StorageSpec): List<File> {
        return listFiles(folder, spec.primaryExtension)
    }

    fun createTimestampedFile(folder: File, rawName: String?, spec: StorageSpec): File {
        val resolvedName = rawName ?: spec.fallbackName
        return createTimestampedFile(folder, resolvedName, spec.fallbackName, spec.primaryExtension)
    }

    fun readDetails(primaryFile: File, spec: StorageSpec): String {
        return spec.detailExtension?.let { readSidecar(primaryFile, it) } ?: ""
    }

    fun writeDetails(primaryFile: File, spec: StorageSpec, content: String) {
        spec.detailExtension?.let { writeSidecar(primaryFile, it, content) }
    }

    fun deleteDetails(primaryFile: File, spec: StorageSpec) {
        spec.detailExtension?.let { deleteSidecar(primaryFile, it) }
    }
}
