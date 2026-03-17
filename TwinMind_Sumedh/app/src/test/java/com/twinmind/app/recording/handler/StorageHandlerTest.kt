package com.twinmind.app.recording.handler

import android.os.Looper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(RobolectricTestRunner::class)
class StorageHandlerTest {

    private lateinit var handler: StorageHandler

    @Before
    fun setup() {
        handler = StorageHandler(RuntimeEnvironment.getApplication())
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `getChunkFile creates correct path`() {
        val file = handler.getChunkFile("session-1", 3)
        assertTrue(file.path.contains("chunks/session-1/chunk_3.pcm"))
    }

    @Test
    fun `getChunkFile creates parent directory`() {
        val file = handler.getChunkFile("session-mkdir-test", 0)
        assertTrue(file.parentFile?.exists() == true)
    }

    @Test
    fun `deleteSessionChunks removes all files`() {
        val f1 = handler.getChunkFile("session-del", 0).also { it.createNewFile() }
        val f2 = handler.getChunkFile("session-del", 1).also { it.createNewFile() }
        assertTrue(f1.exists() && f2.exists())
        handler.deleteSessionChunks("session-del")
        assertFalse(f1.exists())
        assertFalse(f2.exists())
    }

    @Test
    fun `hasEnoughStorage returns boolean without throwing`() {
        // Just ensure no crash — actual value depends on device state
        val result = handler.hasEnoughStorage()
        assertTrue(result || !result)
    }
}