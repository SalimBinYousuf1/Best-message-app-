package com.example.data.local

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object AttachmentStorageManager {
    private const val TAG = "AttachmentStorage"
    private const val ATTACHMENTS_DIR = "attachments"

    /**
     * Persists an attachment from a temporary content URI into durable, app-private internal storage.
     * Guaranteed to survive process death, app restarts, device reboot, and content permission expiry.
     *
     * @param context Application or activity context
     * @param sourceUri The temporary content URI provided by the Photo Picker or GetContent
     * @param extension File extension (e.g. "jpg", "png", "pdf")
     * @return Absolute path of the saved private file, or null if copy failed
     */
    suspend fun persistAttachment(
        context: Context,
        sourceUri: Uri,
        extension: String = "jpg"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val attachmentsDir = File(context.filesDir, ATTACHMENTS_DIR).apply {
                if (!exists()) mkdirs()
            }

            val fileName = "att_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.$extension"
            val targetFile = File(attachmentsDir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: $sourceUri")
                return@withContext null
            }

            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }

            Log.d(TAG, "Successfully persisted attachment to durable storage: ${targetFile.absolutePath} (${targetFile.length()} bytes)")
            targetFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting attachment", e)
            null
        }
    }

    /**
     * Checks if a persistent file path exists and is readable.
     */
    fun fileExists(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val file = File(path)
        return file.exists() && file.length() > 0
    }
}
