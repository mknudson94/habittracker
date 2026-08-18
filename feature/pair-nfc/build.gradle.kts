plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mk.habittracker.feature.pairnfc"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    ksp(libs.hilt.android.compiler)

    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(project(":core:nfc"))

    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.firebase.auth)
    implementation(libs.hilt.android)
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
