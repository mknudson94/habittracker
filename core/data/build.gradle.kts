plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mk.habittracker.core.data"
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
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    ksp(libs.hilt.android.compiler)

    api(libs.dagger)
    api(libs.javax.inject)
    api(project(":core:database"))
    api(project(":core:model"))

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.hilt.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.firebase.bom))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)

    testRuntimeOnly(libs.robolectric)
}
