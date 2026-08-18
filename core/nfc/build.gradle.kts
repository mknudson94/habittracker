plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mk.habittracker.core.nfc"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    ksp(libs.hilt.android.compiler)

    api(libs.androidx.lifecycle.common)
    api(libs.hilt.android)
    api(project(":core:data"))

    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))
    implementation(project(":core:model"))

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
}
