plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mechanicalkeyboard.pro"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mechanicalkeyboard.pro"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.6.0-stage5-6"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    // Needed to host Compose inside an InputMethodService (not an Activity):
    // provides setViewTreeLifecycleOwner / setViewTreeSavedStateRegistryOwner,
    // both confirmed required by real on-device crashes — see
    // ComposeInputMethodService for the full explanation. ViewModelStoreOwner
    // deliberately still not wired up — nothing has asked for it yet.
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.savedstate:savedstate-ktx:1.2.1")

    // Stage 6 — persisted customization (key size, sound/haptic toggles,
    // accent color). See data/repository/SettingsRepository.kt.
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
