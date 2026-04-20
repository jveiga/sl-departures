import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.spotless)
}

tasks.register("checkNoApiKeys") {
    group = "verification"
    description = "Checks if any API keys are hardcoded in the source code."
    doLast {
        val pattern = Regex("(?i)(api_key|apikey|secret|token)\\s*=\\s*\"[a-zA-Z0-9-]{10,}\"")
        var foundKeys = false
        fileTree("src/main/java").forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                if (pattern.containsMatchIn(line)) {
                    println("ERROR: Hardcoded API key found in ${file.path}:${index + 1}")
                    foundKeys = true
                }
            }
        }
        if (foundKeys) {
            throw GradleException("Build failed: Hardcoded API keys detected in source code. Please move them to a secure location.")
        }
    }
}

// Ensure the check runs before building
tasks.named("preBuild") {
    dependsOn("checkNoApiKeys")
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint()
    }
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "veiga.sl.departures"
    compileSdk = 36

    defaultConfig {
        applicationId = "veiga.sl.departures"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "RESROBOT_API_KEY", "\"${localProperties.getProperty("RESROBOT_API_KEY") ?: ""}\"")
        buildConfigField("String", "DEPARTURES_API_KEY", "\"${localProperties.getProperty("DEPARTURES_API_KEY") ?: ""}\"")
        buildConfigField("String", "SL_NEARBY_API_KEY", "\"${localProperties.getProperty("SL_NEARBY_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.compose.icons.core)
    implementation(libs.compose.icons.extended)
    implementation(libs.security.crypto)
    ksp(libs.room.compiler)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
