plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("kotlin-kapt")
}

android {
    namespace = "com.losslessmusic.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.losslessmusic.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "5.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    namespace = "com.losslessmusic.app"
}

dependencies {
    // Core Android
    val coreKtx = "androidx.core:core-ktx:1.12.0"
    implementation("com.google.android.material:material:1.11.0")
    val lifecycleVersion = "2.7.0"
    val activityCompose = "androidx.activity:activity-compose:1.8.2"
    implementation(coreKtx)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation(activityCompose)

    // Hilt DI
    val hiltVersion = "2.48.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")

    // Coil Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // OkHttp & Serialization
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Media3 ExoPlayer
    val media3Version = "1.4.1"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Accompanist (permissions, etc.)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kapt {
    correctErrorTypes = true
}