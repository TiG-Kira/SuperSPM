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
            // 私钥库本体不入仓库（.gitignore 已排除 *.jks）：
            //   CI    —— 由仓库 Secret JKS_BASE64 解码写入该路径
            //   本地   —— 从 Termux Ultra 的 app/ 目录复制一份放到这里
            storeFile = file("ki-terminal-release.jks")
            // 【安全】签名口令一律外置，不得写回本文件：本仓库是公开的，
            // 一旦口令与私钥同时落到明文仓库里，任何人都能签出外观无差的包。
            //
            // 取值顺序：Gradle 属性 → 环境变量 → 空。
            //   CI   ：由 Secrets 注入同名环境变量（见 .github/workflows/release_apk.yml）
            //   本地 ：./gradlew assembleRelease -Psuperspm.storePassword=... -Psuperspm.keyPassword=...
            //        或先 export SUPERSPM_STORE_PASSWORD / SUPERSPM_KEY_PASSWORD
            // 口令留空不会污染 debug 构建，但 release 签名会直接失败——这是刻意的：
            // 宁可构建报错，也不要静默产出一个签名不对的包。
            storePassword = (project.findProperty("superspm.storePassword")
                ?: System.getenv("SUPERSPM_STORE_PASSWORD")
                ?: "").toString()
            keyAlias = (project.findProperty("superspm.keyAlias")
                ?: System.getenv("SUPERSPM_KEY_ALIAS")
                ?: "ki-terminal").toString()
            keyPassword = (project.findProperty("superspm.keyPassword")
                ?: System.getenv("SUPERSPM_KEY_PASSWORD")
                ?: "").toString()
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