plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kira.superspm"
    compileSdk = Versions.compileSdk
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.kira.superspm"
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 5
        versionName = "1.0.5"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    applicationVariants.all {
        val variantName = name
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val fileName = "SuperSPM_${variantName}_${versionName}.apk"
            output.outputFileName = fileName
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.kotlin
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinSerialization)
    implementation(Dependencies.kotlinDatetime)

    implementation(Dependencies.composeUi)
    implementation(Dependencies.composeFoundation)
    implementation(Dependencies.composeMaterial)
    implementation(Dependencies.composeMaterial3)
    implementation(Dependencies.composeMaterialIcons)
    implementation(Dependencies.composeMaterialIconsExtended)
    implementation(Dependencies.composeRuntime)
    implementation(Dependencies.composeActivity)
    debugImplementation(Dependencies.composeUiTooling)

    implementation(Dependencies.miuixUi)
    implementation(Dependencies.miuixIcons)

    implementation(Dependencies.lifecycleViewModel)
    implementation(Dependencies.lifecycleRuntime)

    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomKtx)
    ksp(Dependencies.roomCompiler)

    implementation(Dependencies.datastorePreferences)

    implementation(Dependencies.navCompose)

    implementation(Dependencies.playServicesLocation)

    implementation(Dependencies.timber)

    implementation(Dependencies.koinCore)
    implementation(Dependencies.koinAndroid)
    implementation(Dependencies.koinCompose)

    implementation(Dependencies.coilCompose)
}