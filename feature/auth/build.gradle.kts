plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mk.habittracker.feature.auth"
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
}

dependencies {
    debugImplementation(libs.androidx.compose.ui.tooling)

    ksp(libs.hilt.android.compiler)

    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.ui)
    api(libs.hilt.android)

    // TODO: whatever this credentials squiggly means
    implementation(libs.androidx.credentials)
    implementation(libs.google.googleid)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.firebase.auth)
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform(libs.firebase.bom))

    runtimeOnly(libs.androidx.credentials.play.services.auth)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
}
