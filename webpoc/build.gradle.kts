plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.local.matholickiosk.webpoc"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.local.matholickiosk.webpoc"
        minSdk = 33
        targetSdk = 37
        versionCode = 14
        versionName = "0.3.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
