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
        
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
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
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation)
    
    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.firebase.ui.auth)
    implementation(libs.material)
    // Removed firebase.crashlytics.buildtools to fix 16 KB page size compatibility

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // In your app/build.gradle.kts dependencies block
    implementation("androidx.compose.material3:material3:1.3.2") // Or a newer stable/beta version

}