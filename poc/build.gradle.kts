plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.local.matholickiosk.poc"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.local.matholickiosk.poc"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-locked"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    testImplementation(libs.junit4)
}
