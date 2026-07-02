plugins {
    kotlin("jvm") version "2.3.21"
    application
}

application {
    mainClass = "io.github.takahirom.clijvm.MainKt"
}

group = "io.github.takahirom"
// Release versions come from the git tag via CI (-PclijvmVersion=<tag>); local builds are "dev".
version = (findProperty("clijvmVersion") as String?) ?: "0.0.0-dev"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:4.4.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.distTar {
    compression = Compression.GZIP
    archiveExtension = "tar.gz"
}

tasks.test {
    useJUnitPlatform()
    // Integration tests attach to a spawned child JVM; give them room and their own reporting.
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }
}
