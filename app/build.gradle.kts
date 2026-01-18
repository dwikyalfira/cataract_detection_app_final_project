plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.dicoding.cataract_detection_app_final_project"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dicoding.cataract_detection_app_final_project"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        mlModelBinding = true
    }
    
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    ndkVersion = "26.1.10909125"
}

dependencies {
    // AppCompat for backward compatibility
    implementation(libs.androidx.appcompat)
    
    // Core KTX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    
    // DataStore Preferences
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.13.2")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.firebase.ui.auth)
    implementation(libs.material)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)

    // Retrofit for Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation(libs.androidx.room.external.antlr)
    // Removed firebase.crashlytics.buildtools to fix 16 KB page size compatibility

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // In your app/build.gradle.kts dependencies block
    implementation("androidx.compose.material3:material3:1.4.0") // Or a newer stable/beta version

    // TensorFlow Lite (LiteRT) dependencies
    implementation("com.google.ai.edge.litert:litert:1.0.1")
    implementation("org.tensorflow:tensorflow-lite-support:0.5.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.5.0")
    // GPU Delegate (optional, but good for performance)
    implementation("com.google.ai.edge.litert:litert-gpu:1.0.1")

    // Image Cropper
    implementation("com.vanniktech:android-image-cropper:4.6.0")

    // ExifInterface for image orientation handling
    implementation("androidx.exifinterface:exifinterface:1.3.7")

}