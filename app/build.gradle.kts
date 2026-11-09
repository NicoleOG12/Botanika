plugins {
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.android.application") version "9.3.3"
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.botanika"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.botanika"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    // REMOVED: kotlinOptions block that caused the error
}

// ADDED: Modern compiler options block for AGP 9+ / built-in Kotlin
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    implementation("com.google.code.gson:gson:2.10.1")
    // 2. Dependências do Firebase corretas
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-appcheck-debug")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room3.common)
    implementation(libs.androidx.room3.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Room (Runtime e KSP Compiler corretos nas dependências)
    implementation(libs.androidx.room3.runtime) // Ou a sua dependência de runtime do Room
    ksp("androidx.room3:room3-compiler:3.0.3") // Mude para a versão exata do Room que você está utilizando (ex: 2.6.1)

}