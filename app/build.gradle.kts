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
        versionCode = 18
        versionName = "1.2.6"
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:\\KiTerminal-UX\\app\\ki-terminal-release.jks")
            storePassword = "ki-terminal"
            keyAlias = "ki-terminal"
            keyPassword = "ki-terminal"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
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
    implementation(Dependencies.miuixBlur)
    implementation(Dependencies.miuixIcons)

    // miuix-glass 弹层控件（GlassDropdownPopup / GlassPopup 等）依赖
    // NavigationBackHandler / rememberNavigationEventState，1.1.2 起提供
    implementation(Dependencies.navigationEvent)

    // Liquid Glass 效果（Android-only artifacts）。
    // 排除 org.jetbrains.compose.* 传递依赖：它们是 KMP 侧的 androidx.compose 别名，
    // 与本项目的 androidx.compose 1.12.0 重复，留着会 duplicate class。
    implementation(Dependencies.backdrop) {
        exclude(group = "org.jetbrains.compose")
        exclude(group = "org.jetbrains.compose.ui")
        exclude(group = "org.jetbrains.compose.foundation")
    }
    implementation(Dependencies.kyantShapes) {
        exclude(group = "org.jetbrains.compose")
        exclude(group = "org.jetbrains.compose.ui")
        exclude(group = "org.jetbrains.compose.foundation")
    }

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

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation(project(":plugin-sdk"))
}