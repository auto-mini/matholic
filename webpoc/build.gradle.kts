plugins {
    alias(libs.plugins.android.application)
}

val releaseStoreFile = providers.environmentVariable("MATHOLIC_RELEASE_STORE_FILE").orNull
val releaseStorePassword =
    providers.environmentVariable("MATHOLIC_RELEASE_STORE_PASSWORD").orNull
val releaseKeyAlias = providers.environmentVariable("MATHOLIC_RELEASE_KEY_ALIAS").orNull
val releaseKeyPassword =
    providers.environmentVariable("MATHOLIC_RELEASE_KEY_PASSWORD").orNull
val releaseSigningConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.local.matholickiosk.webpoc"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.local.matholickiosk.webpoc"
        minSdk = 33
        targetSdk = 37
        versionCode = 19
        versionName = "0.3.5-rc02"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(checkNotNull(releaseStoreFile))
                storePassword = checkNotNull(releaseStorePassword)
                keyAlias = checkNotNull(releaseKeyAlias)
                keyPassword = checkNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
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
    implementation(libs.androidx.webkit)
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
}

tasks.configureEach {
    if (name.contains("Release", ignoreCase = false)) {
        doFirst {
            check(releaseSigningConfigured) {
                "Release signing is required. Use scripts/build-release.ps1."
            }
        }
    }
}
