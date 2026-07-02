package io.github.takahirom.clijvm.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.versionOption

/** Root command: `clijvm`. */
class ClijvmCommand : CliktCommand(
    name = "clijvm",
    help = "AI-friendly JVM profiler powered by JDK Flight Recorder.",
    printHelpOnEmptyArgs = true,
) {
    init {
        versionOption(clijvmVersion())
    }

    override fun run() = Unit
}

/**
 * The build version, read from the jar manifest's `Implementation-Version` (stamped by Gradle from
 * the release tag). Falls back to `dev` for classpath/IDE runs without a packaged manifest.
 */
fun clijvmVersion(): String =
    ClijvmCommand::class.java.`package`?.implementationVersion ?: "dev"
