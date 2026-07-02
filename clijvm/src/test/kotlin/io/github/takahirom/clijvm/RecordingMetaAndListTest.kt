package io.github.takahirom.clijvm

import io.github.takahirom.clijvm.cli.MAX_DISPLAY_NAME
import io.github.takahirom.clijvm.cli.truncateDisplayName
import io.github.takahirom.clijvm.util.RecordingMeta
import io.github.takahirom.clijvm.util.readRecordingMeta
import io.github.takahirom.clijvm.util.recordingMetaPath
import io.github.takahirom.clijvm.util.writeRecordingMeta
import java.nio.file.Files
import kotlin.io.path.deleteRecursively
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecordingMetaAndListTest {

    @Test
    fun `short display names are not truncated`() {
        val (text, truncated) = truncateDisplayName("com.example.Main", full = false)
        assertEquals("com.example.Main", text)
        assertFalse(truncated)
    }

    @Test
    fun `long display names are truncated with an ellipsis unless full`() {
        val long = "x".repeat(MAX_DISPLAY_NAME + 50)
        val (text, truncated) = truncateDisplayName(long, full = false)
        assertTrue(truncated)
        assertEquals(MAX_DISPLAY_NAME + 1, text.length) // MAX chars + the ellipsis
        assertTrue(text.endsWith("…"))

        val (fullText, fullTruncated) = truncateDisplayName(long, full = true)
        assertEquals(long, fullText)
        assertFalse(fullTruncated)
    }

    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    @Test
    fun `recording meta round-trips and tolerates a missing sidecar`() {
        val dir = Files.createTempDirectory("clijvm-meta")
        try {
            val recording = dir.resolve("20260101-120000-4242.jfr")
            Files.writeString(recording, "not a real jfr")

            assertNull(readRecordingMeta(recording), "no sidecar yet")

            val meta = RecordingMeta(pid = 4242, mainClass = "com.example.\"Main\"", startedAt = 1_700_000_000_000, partial = true)
            writeRecordingMeta(recording, meta)
            assertTrue(Files.exists(recordingMetaPath(recording)))

            val read = readRecordingMeta(recording)
            assertEquals(meta, read)
        } finally {
            dir.deleteRecursively()
        }
    }

    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    @Test
    fun `corrupt sidecar is ignored rather than throwing`() {
        val dir = Files.createTempDirectory("clijvm-meta-corrupt")
        try {
            val recording = dir.resolve("rec.jfr")
            Files.writeString(recording, "jfr")
            Files.writeString(recordingMetaPath(recording), "{ this is not json")
            assertNull(readRecordingMeta(recording))
        } finally {
            dir.deleteRecursively()
        }
    }
}
