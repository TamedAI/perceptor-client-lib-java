import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.bundling.Jar

plugins {
    kotlin("jvm") version "1.8.20"
    application
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.21"
    id("org.jetbrains.dokka") version "1.9.0"
    `maven-publish`
}

val artifactGroupId = "org.tamedai"
group = artifactGroupId

val explicitVersion: String? by project
val versionSuffix: String? by project

val baseVersion = "0.5.0"

val effectiveSuffix = when{
    versionSuffix.isNullOrBlank() -> ""
    else -> versionSuffix
}
version = when{
    explicitVersion.isNullOrBlank() -> "$baseVersion$effectiveSuffix"
    else -> explicitVersion!!
}

println("version is: '$version'")


repositories {
    mavenCentral()
}

val kotestVersion = "5.8.0"
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("io.github.resilience4j:resilience4j-retry:1.7.0")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.mockk:mockk:1.13.8")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    testLogging.events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED)
    useJUnitPlatform()
}

tasks.dokkaJavadoc {
    outputDirectory.set(buildDir.resolve("docs/markdown"))
}

tasks.register<Jar>("dokkaJavadocJar") {

    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}


kotlin {
    jvmToolchain(11)

}

application {
    mainClass.set("MainKt")
}
