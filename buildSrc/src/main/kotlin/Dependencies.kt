object Versions {
    // backdrop-android 2.0.0（液态玻璃效果底座）的 AAR 元数据要求 minCompileSdk=37。
    // 本地已装 android-37 平台，故 compileSdk 提到 37；targetSdk 保持 36 不动，
    // 避免运行期行为变更。AGP 8.10 官方只推荐到 36，需配合
    // gradle.properties 里的 android.suppressUnsupportedCompileSdk=37。
    const val compileSdk = 37
    const val minSdk = 26
    const val targetSdk = 36

    const val kotlin = "2.3.20"
    // miuix 0.9.4 起 POM 经 org.jetbrains.compose 传递依赖 compose 1.12.0，
    // 而 compose 1.12 的 AAR 元数据要求 AGP 9.1+（本项目 AGP 8.10）。
    // 因此取 miuix 0.9.3（依赖 compose 1.11.1，AGP 8.10 可消费），
    // 玻璃组件库源码与 miuix 0.9.3 的 blur/shader/anim API 已逐一验证兼容。
    const val compose = "1.11.1"
    const val miuix = "0.9.3"
    const val lifecycle = "2.8.7"
    const val room = "2.7.0"
    const val datastore = "1.0.0"
    const val nav = "2.8.3"
}

object Dependencies {
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3"
    const val kotlinDatetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.6.0"

    const val coilCompose = "io.coil-kt:coil-compose:2.5.0"

    const val composeCompiler = "androidx.compose.compiler:compiler:${Versions.kotlin}"
    const val composeUi = "androidx.compose.ui:ui:${Versions.compose}"
    const val composeUiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val composeFoundation = "androidx.compose.foundation:foundation:${Versions.compose}"
    const val composeMaterial3 = "androidx.compose.material3:material3:1.4.0"
    const val composeMaterial = "androidx.compose.material:material:${Versions.compose}"
    const val composeMaterialIcons = "androidx.compose.material:material-icons-core:1.7.8"
    const val composeMaterialIconsExtended = "androidx.compose.material:material-icons-extended:1.7.8"

    const val composeRuntime = "androidx.compose.runtime:runtime:${Versions.compose}"
    const val composeActivity = "androidx.activity:activity-compose:1.9.3"

    // miuix 0.9.x 起 UI 主 artifact 更名为 miuix-ui-android；blur/shader 为玻璃拟态组件依赖
    const val miuixUi = "top.yukonga.miuix.kmp:miuix-ui-android:${Versions.miuix}"
    const val miuixBlur = "top.yukonga.miuix.kmp:miuix-blur-android:${Versions.miuix}"
    const val miuixIcons = "top.yukonga.miuix.kmp:miuix-icons-android:${Versions.miuix}"

    // miuix-glass 弹层控件需要 NavigationBackHandler / rememberNavigationEventState（1.1.2 起提供）
    const val navigationEvent = "androidx.navigationevent:navigationevent-compose-android:1.1.2"

    // Liquid Glass 效果（kyant0）：液态玻璃导航栏与形状。
    // 版本与 backdrop 2.0.0 的 POM 依赖严格对齐（shapes 1.2.1 起 minCompileSdk=37，AGP 8.10 用不了）。
    const val backdrop = "io.github.kyant0:backdrop-android:2.0.0"
    const val kyantShapes = "io.github.kyant0:shapes-android:1.2.0"

    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.lifecycle}"
    const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}"

    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"

    const val datastorePreferences = "androidx.datastore:datastore-preferences:${Versions.datastore}"

    const val navFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.nav}"
    const val navUi = "androidx.navigation:navigation-ui-ktx:${Versions.nav}"
    const val navCompose = "androidx.navigation:navigation-compose:${Versions.nav}"

    const val playServicesLocation = "com.google.android.gms:play-services-location:21.4.0"

    const val timber = "com.jakewharton.timber:timber:5.0.1"

    const val koinCore = "io.insert-koin:koin-core:3.5.6"
    const val koinAndroid = "io.insert-koin:koin-android:3.5.6"
    const val koinCompose = "io.insert-koin:koin-androidx-compose:3.5.6"
}
