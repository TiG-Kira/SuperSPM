object Versions {
    const val compileSdk = 36
    const val minSdk = 26
    const val targetSdk = 36

    const val kotlin = "2.3.20"
    const val compose = "1.8.3"
    const val miuix = "0.8.5"
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
    const val composeMaterial3 = "androidx.compose.material3:material3:1.3.0"
    const val composeMaterial = "androidx.compose.material:material:${Versions.compose}"
    const val composeMaterialIcons = "androidx.compose.material:material-icons-core:1.7.4"
    const val composeMaterialIconsExtended = "androidx.compose.material:material-icons-extended:1.7.4"
    
    const val composeRuntime = "androidx.compose.runtime:runtime:${Versions.compose}"
    const val composeActivity = "androidx.activity:activity-compose:1.9.3"

    const val miuixUi = "top.yukonga.miuix.kmp:miuix-android:${Versions.miuix}"
    const val miuixIcons = "top.yukonga.miuix.kmp:miuix-icons-android:${Versions.miuix}"

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
