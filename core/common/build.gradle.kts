plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.mk.habittracker.core.common"
    compileSdk = 37

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
