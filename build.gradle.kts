plugins {
    kotlin("multiplatform") version "2.1.20"
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
            }
        }
        val macosArm64Main by getting
    }
}

