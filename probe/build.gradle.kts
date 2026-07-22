plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.local.matholickiosk.probe"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.local.matholickiosk.probe"
        minSdk = 33
        targetSdk = 37
        versionCode = 2
        versionName = "0.1.1-gate1"
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

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = false
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = false
    }
}

dependencies {
    testImplementation(libs.junit4)
}
