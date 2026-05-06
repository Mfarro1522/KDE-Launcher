plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
}

val releaseKeystorePath = providers.environmentVariable("TAPO_RELEASE_KEYSTORE_PATH")
val releaseStorePassword = providers.environmentVariable("TAPO_RELEASE_STORE_PASSWORD")
val releaseKeyAlias = providers.environmentVariable("TAPO_RELEASE_KEY_ALIAS")
val releaseKeyPassword = providers.environmentVariable("TAPO_RELEASE_KEY_PASSWORD")

val isReleaseSigningConfigured = listOf(
    releaseKeystorePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.orNull.isNullOrBlank() }

android {
    namespace = "dev.vive.kdelauncher"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.vive.kdelauncher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            if (isReleaseSigningConfigured) {
                storeFile = file(releaseKeystorePath.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
                enableV1Signing = true
                enableV2Signing = true
            } else {
                // Dummy signing for local builds (CI uses real signing)
                storeFile = file("debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Unit tests
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

tasks.configureEach {
    val isReleasePackagingTask = name.startsWith("assembleRelease") || name.startsWith("bundleRelease")
    if (isReleasePackagingTask && !file("debug.keystore").exists()) {
        doFirst {
            check(isReleaseSigningConfigured) {
                "Release signing is not configured. Set TAPO_RELEASE_KEYSTORE_PATH, TAPO_RELEASE_STORE_PASSWORD, TAPO_RELEASE_KEY_ALIAS, and TAPO_RELEASE_KEY_PASSWORD."
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kover {
    reports {
        total {
            html { onCheck = true }
            xml { onCheck = true }
        }
        verify {
            rule {
                bound {
                    minValue = 20
                }
            }
        }
        filters {
            excludes {
                classes(
                    "*.generated.*",
                    "*.R$*",
                    "*.BuildConfig",
                    "dev.vive.kdelauncher.ui.*",
                    "dev.vive.kdelauncher.MainActivity*",
                    "dev.vive.kdelauncher.KDELauncherApp*",
                    "dev.vive.kdelauncher.SetDefaultLauncherActivity*",
                )
            }
        }
    }
}
