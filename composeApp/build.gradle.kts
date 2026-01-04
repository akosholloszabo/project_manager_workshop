import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            val lwjglVersion = "3.3.1"
            listOf("lwjgl", "lwjgl-nfd").forEach { lwjglDep ->
                implementation("org.lwjgl:${lwjglDep}:${lwjglVersion}")
                listOf(
                    "natives-windows", "natives-windows-x86", "natives-windows-arm64",
                    "natives-macos", "natives-macos-arm64",
                    "natives-linux", "natives-linux-arm64", "natives-linux-arm32"
                ).forEach { native ->
                    runtimeOnly("org.lwjgl:${lwjglDep}:${lwjglVersion}:${native}")
                }
            }

            val mikpezVersion = "0.38.1"
            // Core library
            implementation("com.mikepenz:multiplatform-markdown-renderer:${mikpezVersion}")
// OR for Material 3 themed apps
            implementation("com.mikepenz:multiplatform-markdown-renderer-m3:${mikpezVersion}")
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientCio)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientSerialization)
            implementation(libs.ktor.certificates)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}


compose.desktop {
    application {
        mainClass = "hu.akosholloszabo.project_manager.project_manager_workshop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "hu.akosholloszabo.project_manager.project_manager_workshop"
            packageVersion = "1.0.0"
        }
    }
}
