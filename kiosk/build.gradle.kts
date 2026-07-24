plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
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
    namespace = "com.local.matholickiosk.kiosk"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.local.matholickiosk.kiosk"
        minSdk = 33
        targetSdk = 37
        versionCode = 7
        versionName = "0.5.0-rc02"
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

    packaging {
        resources.excludes += setOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE.md",
            "META-INF/NOTICE.md",
        )
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.room.runtime)
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.zxing.core)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.room.testing)
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
