package io.github.takahirom.clijvm.util

import java.nio.file.Files
import java.nio.file.Path

/**
 * Sidecar metadata written next to a `.jfr` recording as `<recording>.meta.json`.
 *
 * It lets `report` recover facts the raw recording does not carry reliably: the target's main
 * class (JFR often reports it as unknown) and whether the recording was a salvaged PARTIAL dump.
 * The sidecar is best-effort — a missing or corrupt file simply falls back to filename heuristics.
 */
data class RecordingMeta(
    val pid: Long?,
    val mainClass: String?,
    /** When profiling started, in epoch milliseconds. */
    val startedAt: Long?,
    val partial: Boolean,
)

/** Resolves the sidecar path for [recording]: `<recording>.meta.json`. */
fun recordingMetaPath(recording: Path): Path =
    recording.resolveSibling("${recording.fileName}.meta.json")

/** Writes [meta] next to [recording]; failures are swallowed since the sidecar is best-effort. */
fun writeRecordingMeta(recording: Path, meta: RecordingMeta) {
    val json = Json.Obj(
        listOf(
            "pid" to (meta.pid?.let { jsonInt(it) } ?: Json.Literal("null")),
            "mainClass" to jsonStringOrNull(meta.mainClass),
            "startedAt" to (meta.startedAt?.let { jsonInt(it) } ?: Json.Literal("null")),
            "partial" to jsonBool(meta.partial),
        )
    ).render()
    runCatching { Files.writeString(recordingMetaPath(recording), json) }
}

/** Reads the sidecar for [recording], or null when it is absent or not well-formed. */
fun readRecordingMeta(recording: Path): RecordingMeta? {
    val path = recordingMetaPath(recording)
    if (!Files.isReadable(path)) return null
    return runCatching {
        val obj = JsonParser.parse(Files.readString(path)) as? Map<*, *> ?: return null
        RecordingMeta(
            pid = (obj["pid"] as? Double)?.toLong(),
            mainClass = obj["mainClass"] as? String,
            startedAt = (obj["startedAt"] as? Double)?.toLong(),
            partial = obj["partial"] as? Boolean ?: false,
        )
    }.getOrNull()
}
