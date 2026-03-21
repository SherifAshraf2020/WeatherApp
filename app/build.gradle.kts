import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.weatherapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.weatherapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        val apiKey = localProperties.getProperty("WEATHER_API_KEY") ?: ""

        buildConfigField("String", "API_KEY", "\"$apiKey\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material) // Required for PullRefresh

    testImplementation(libs.junit)
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // --- Networking (Retrofit & API Integration) ---
    implementation(libs.retrofit)               // Core library for HTTP requests
    implementation(libs.converter.gson)         // JSON to Kotlin Objects converter
    implementation(libs.gson)                   // Google's JSON processing library

    // --- Local Storage (Room Database) ---
    implementation(libs.room.runtime)           // Room persistence library
    implementation(libs.room.ktx)               // Coroutines support for Room
    ksp(libs.room.compiler)                     // Annotation processor (KSP)

    //--- Splash Screen ---
    implementation(libs.androidx.core.splashscreen)

    // Dependency for using viewModel() in Composable functions and handling ViewModelFactory
    implementation(libs.androidx.lifecycle.viewmodel.compose.android)

    // Library for extended Material icons like WbSunny and Schedule
    implementation(libs.androidx.compose.material.icons.extended)

    // --- Location Services ---
    implementation(libs.play.services.location)

    // Image Loading Library (Coil) for Weather Icons
    implementation(libs.coil.compose)

    // free OpenStreetMap Android Library for Android
    implementation(libs.osmdroid.android)

    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.places)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization)

    val work_version = "2.9.0"
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.androidx.core.ktx.v1120)


    // --- 1. Local Unit Tests (Directly for your Business Logic) ---
    testImplementation(libs.core.ktx)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit.ktx)
    testImplementation(libs.jetbrains.kotlinx.coroutines.test)
    testImplementation(libs.core.testing)
    testImplementation(libs.mockk)

    // --- 2. Instrumented Tests (UI & Framework Integration) ---
    androidTestImplementation(libs.core.testing)
    androidTestImplementation(libs.jetbrains.kotlinx.coroutines.test)

    // --- 3. Kotlin & Extensions ---
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.material)
}
