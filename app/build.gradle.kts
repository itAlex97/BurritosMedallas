plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.gutierrez_rodriguez.burritosmedallas"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.gutierrez_rodriguez.burritosmedallas"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Librería moderna para Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
    // FirebaseUI para Cloud Firestore (Facilita el RecyclerView)
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")
    implementation("com.google.android.material:material:1.9.0")
}