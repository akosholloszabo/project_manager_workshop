package hu.akosholloszabo.project_manager.project_manager_workshop

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.nfd.NativeFileDialog
import javax.swing.JFileChooser
import javax.swing.UIManager

object FileChooser {
    private val userHomePath = System.getProperty("user.home")

    suspend fun chooseDirectory(): String? {
        return runCatching { chooseDirectoryNative() }
            .onFailure { nativeException ->
                println("A call to chooseDirectoryNative failed: ${nativeException.message}")

                return runCatching { chooseDirectorySwing() }
                    .onFailure { swingException ->
                        println("A call to chooseDirectorySwing failed ${swingException.message}")
                    }
                    .getOrNull()
            }
            .getOrNull()
    }

    private suspend fun chooseDirectoryNative() = withContext(Dispatchers.IO) {
        val pathPointer = MemoryUtil.memAllocPointer(1)
        try {
            return@withContext when (val code = NativeFileDialog.NFD_PickFolder(userHomePath, pathPointer)) {
                NativeFileDialog.NFD_OKAY -> {
                    val path = pathPointer.stringUTF8
                    NativeFileDialog.nNFD_Free(pathPointer[0])

                    path
                }

                NativeFileDialog.NFD_CANCEL -> null
                NativeFileDialog.NFD_ERROR -> error("An error occurred while executing NativeFileDialog.NFD_PickFolder")
                else -> error("Unknown return code '${code}' from NativeFileDialog.NFD_PickFolder")
            }
        } finally {
            MemoryUtil.memFree(pathPointer)
        }
    }

    private suspend fun chooseDirectorySwing() = withContext(Dispatchers.IO) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        val chooser = JFileChooser(userHomePath).apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isVisible = true
        }

        when (val code = chooser.showOpenDialog(null)) {
            JFileChooser.APPROVE_OPTION -> chooser.selectedFile.absolutePath
            JFileChooser.CANCEL_OPTION -> null
            JFileChooser.ERROR_OPTION -> error("An error occurred while executing JFileChooser::showOpenDialog")
            else -> error("Unknown return code '${code}' from JFileChooser::showOpenDialog")
        }
    }
}
