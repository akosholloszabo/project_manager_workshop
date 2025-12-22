package hu.akosholloszabo.project_manager.project_manager_workshop.storage

import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDateTime.now
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Locale.US

object FileStorageHelper {
    val defaultJson = Json { encodeDefaults = true; prettyPrint = true }

    private val timestampFormatter = ofPattern("yyyyMMdd-HHmmss", US)

    data class StorageSpec(
        val folderName: String,
        val primaryExtension: String,
        val fallbackName: String,
        val detailExtension: String? = null
    )

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

    private fun normalizeExtension(extension: String): String {
        return extension.trimStart('.')
    }

    private fun ensureDotPrefix(extension: String): String {
        val normalized = normalizeExtension(extension)
        return ".$normalized"
    }

    fun ensureStorageDirectory(root: String?, spec: StorageSpec): File? {
        return root?.let { File(it, spec.folderName) }
            ?.takeIf { it.exists() || it.mkdirs() }
    }

    fun listStorageFiles(folder: File, spec: StorageSpec): List<File> {
        val normalizedExtension = normalizeExtension(spec.primaryExtension)
        return folder.listFiles { file ->
            file.isFile && file.extension.equals(normalizedExtension, ignoreCase = true)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun sanitizeName(rawName: String?, fallback: String): String {
        val resolved = rawName ?: fallback
        return resolved.trim().ifEmpty { fallback }
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .replace(Regex("\\s+"), "-")
            .trim('-')
            .ifEmpty { fallback }
    }

    fun createTimestampedFile(folder: File, rawName: String?, spec: StorageSpec): File {
        val sanitized = sanitizeName(rawName, spec.fallbackName)
        val timestamp = now().format(timestampFormatter)
        val normalizedExtension = ensureDotPrefix(spec.primaryExtension)
        return File(folder, "$sanitized-$timestamp$normalizedExtension")
    }

    fun readDetails(primaryFile: File, spec: StorageSpec): String {
        return spec.detailExtension?.let {
            runCatching {
                ensureSidecarFile(primaryFile, it).readText()
            }.getOrDefault("")
        } ?: ""
    }

    fun writeDetails(primaryFile: File, spec: StorageSpec, content: String) {
        spec.detailExtension?.let {
            ensureSidecarFile(primaryFile, it).writeText(content)
        }
    }

    fun deleteDetails(primaryFile: File, spec: StorageSpec) {
        spec.detailExtension?.let {
            getSidecarFile(primaryFile, it).delete()
        }
    }
}
