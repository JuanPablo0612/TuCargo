import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

val secrets = Properties().apply {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.exists()) {
        load(secretsFile.inputStream())
    }
}

kotlin {
    target {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    dependencies {
        implementation(projects.composeApp)
        implementation(libs.ui.tooling.preview)
        implementation(libs.androidx.activity.compose)
        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.android)
    }
}

android {
    namespace = "com.juanpablo0612.tucargo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.juanpablo0612.tucargo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = secrets.getProperty("GOOGLE_MAPS_API_KEY") ?: ""
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
