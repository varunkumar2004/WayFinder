plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    id("com.google.devtools.ksp")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.varunkumar.wayfinder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.varunkumar.wayfinder"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {}

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Dagger Hilt (use KSP instead of KAPT)
    implementation("com.google.dagger:hilt-android:2.49")
    ksp("com.google.dagger:hilt-compiler:2.49")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.31.0-alpha")

    // Google Maps
    implementation(libs.play.services.location)
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.maps.android:maps-compose:2.11.0")
    implementation("com.google.maps.android:android-maps-utils:3.10.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Compose + AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.android)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
