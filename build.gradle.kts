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
        val commonMain by getting
        val macosArm64Main by getting
    }
}