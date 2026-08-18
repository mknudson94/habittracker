plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mk.habittracker.core.database"
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
    ksp(libs.androidx.room.compiler)

    api(libs.androidx.room.runtime)
    api(project(":core:model"))

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
