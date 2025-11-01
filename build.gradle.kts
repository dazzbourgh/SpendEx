plugins {
    kotlin("multiplatform") version "2.1.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "leonoid"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("io.arrow-kt:arrow-core:1.2.4")
                implementation("com.github.ajalt.clikt:clikt:4.2.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
            }
        }
        val macosArm64Main by getting
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    verbose.set(true)
    outputToConsole.set(true)
    coloredOutput.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

// Auto-format on build
tasks.named("check") {
    dependsOn("ktlintFormat")
}

tasks.named("build") {
    dependsOn("check")
}
