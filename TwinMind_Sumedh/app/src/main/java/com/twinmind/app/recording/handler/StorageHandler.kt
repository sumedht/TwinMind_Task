package com.twinmind.app.recording.handler

import android.content.Context
import android.os.StatFs
import com.twinmind.app.recording.RecordingConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class StorageHandler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasEnoughStorage(): Boolean {
        val stat = StatFs(context.filesDir.path)
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return free >= RecordingConstants.MIN_FREE_STORAGE_BYTES
    }

    fun getChunkFile(sessionId: String, chunkIndex: Int): File {
        val dir = File(context.filesDir, "chunks/$sessionId").also { it.mkdirs() }
        return File(dir, "chunk_${chunkIndex}.pcm")
    }

    fun deleteSessionChunks(sessionId: String) {
        File(context.filesDir, "chunks/$sessionId").deleteRecursively()
    }
}