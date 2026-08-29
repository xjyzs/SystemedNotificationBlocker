plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.xjyzs.systemednotificationblocker"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.xjyzs.systemednotificationblocker"
        minSdk = 26
        targetSdk = 37
        versionCode = 5
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        androidResources.localeFilters += listOf("zh-rCN","en")
    }
    signingConfigs {
        val hasSigningInfo =
            !System.getenv("KEY_STORE_PASSWORD").isNullOrBlank() && !System.getenv("KEY_ALIAS")
                .isNullOrBlank() && !System.getenv("KEY_PASSWORD")
                .isNullOrBlank() && file("${project.rootDir}/keystore.jks").exists()
        if (hasSigningInfo) {
            create("release") {
                storeFile = file("${project.rootDir}/keystore.jks")
                storePassword = System.getenv("KEY_STORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
                enableV1Signing = false
            }
        }
    }

    flavorDimensions += "abi"
    productFlavors {
        create("x86") {
            dimension = "abi"
            ndk { abiFilters.add("x86") }
            this.signingConfig = signingConfig
        }
        create("x86_64") {
            dimension = "abi"
            ndk { abiFilters.add("x86_64") }
            this.signingConfig = signingConfig
        }
        create("arm") {
            dimension = "abi"
            ndk { abiFilters.add("armeabi-v7a") }
            this.signingConfig = signingConfig
        }
        create("arm64Minsdk35") {
            dimension = "abi"
            ndk { abiFilters.add("arm64-v8a") }
            minSdk = 35
            this.signingConfig = signingConfig
        }
        create("arm64Minsdk29") {
            dimension = "abi"
            ndk { abiFilters.add("arm64-v8a") }
            minSdk = 29
            this.signingConfig = signingConfig
        }
        create("arm64Minsdk26") {
            dimension = "abi"
            ndk { abiFilters.add("arm64-v8a") }
            minSdk = 26
            this.signingConfig = signingConfig
        }
        create("universal") {
            dimension = "abi"
            this.signingConfig = signingConfig
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig =
                signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            packaging {
                resources {
                    excludes += setOf(
                        "DebugProbesKt.bin",
                        "kotlin-tooling-metadata.json",
                        "META-INF/**",
                        "kotlin/**"
                    )
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.savedstate.ktx)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.material.icons.extended)

    compileOnly(files("lib/XposedBridgeAPI-89.jar"))
}
