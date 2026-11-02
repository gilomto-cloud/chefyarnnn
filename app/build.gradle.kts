import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
    id("com.google.gms.google-services")
}

val releaseSigningPropertiesFile = rootProject.file("keystore.properties")
val releaseSigningProperties = Properties().apply {
    if (releaseSigningPropertiesFile.isFile) {
        releaseSigningPropertiesFile.inputStream().use(::load)
    }
}
val releaseSigningValues = mapOf(
    "storeFile" to (System.getenv("ANDROID_KEYSTORE_FILE")
        ?: releaseSigningProperties.getProperty("storeFile")),
    "storePassword" to (System.getenv("ANDROID_KEYSTORE_PASSWORD")
        ?: releaseSigningProperties.getProperty("storePassword")),
    "keyAlias" to (System.getenv("ANDROID_KEY_ALIAS")
        ?: releaseSigningProperties.getProperty("keyAlias")),
    "keyPassword" to (System.getenv("ANDROID_KEY_PASSWORD")
        ?: releaseSigningProperties.getProperty("keyPassword"))
)
val hasReleaseSigningValues = releaseSigningValues.values.any { !it.isNullOrBlank() }
val configuredVersionCode = providers.gradleProperty("appVersionCode").orNull
val appVersionCode = configuredVersionCode?.toIntOrNull()
    ?: if (configuredVersionCode == null) 1 else error("appVersionCode must be an integer.")
val appVersionName = providers.gradleProperty("appVersionName").getOrElse("1.0")
val backupAiWorkerUrl = providers.gradleProperty("backupAiWorkerUrl")
    .getOrElse("https://chefyarnnn-backup-ai.gilomto.workers.dev")

check(appVersionCode > 0) { "appVersionCode must be greater than zero." }
check(appVersionName.isNotBlank()) { "appVersionName must not be blank." }

android {
    namespace = "com.trynite.chefyarnnn"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.trynite.chefyarnnn"
        minSdk = 28
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
        buildConfigField("String", "BACKUP_AI_WORKER_URL", "\"$backupAiWorkerUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigningValues) {
            create("release") {
                val missingProperties = releaseSigningValues
                    .filterValues { it.isNullOrBlank() }
                    .keys
                check(missingProperties.isEmpty()) {
                    "Missing release signing values: ${missingProperties.joinToString()}. " +
                        "Set the ANDROID_KEYSTORE_* environment variables or keystore.properties."
                }

                storeFile = rootProject.file(releaseSigningValues.getValue("storeFile")!!)
                storePassword = releaseSigningValues.getValue("storePassword")
                keyAlias = releaseSigningValues.getValue("keyAlias")
                keyPassword = releaseSigningValues.getValue("keyPassword")
                check(storeFile?.isFile == true) {
                    "Release keystore does not exist at ${storeFile?.path}"
                }
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigningValues) {
                signingConfig = signingConfigs.getByName("release")
            }
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    implementation("com.google.firebase:firebase-ai:18.1.0")
    implementation("com.google.firebase:firebase-auth") {
        version { strictly("24.2.0") }
    }
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.googleid)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.converter.moshi)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation("com.google.firebase:firebase-appcheck-debug")
    "ksp"(libs.androidx.room.compiler)
    "ksp"(libs.moshi.kotlin.codegen)
}