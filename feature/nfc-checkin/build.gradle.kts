plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mk.habittracker.nfccheckin"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    ksp(libs.androidx.hilt.compiler)
    ksp(libs.hilt.android.compiler)

    api(libs.androidx.hilt.work)
    api(libs.androidx.work.runtime)
    api(libs.hilt.android)
    api(project(":core:data"))
    api(project(":core:nfc"))

    compileOnly(libs.androidx.hilt.common)

    implementation(libs.androidx.appcompat)
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    androidTestImplementation(libs.androidx.junit)

    testImplementation(libs.androidx.testcore)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
}
