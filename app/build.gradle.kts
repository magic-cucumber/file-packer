import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import util.Platform
import util.platform

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    alias(libs.plugins.kotlinx.serialization)
}

logger.lifecycle("Platform: $platform")
logger.lifecycle("jar build enabled: $build_jvm")
logger.lifecycle("native build type: $build_platform")

inline fun allow(target: Platform, block: () -> Unit) =
    if (build_platform.isAll || (build_platform.isPlatform && platform == target)) block() else Unit

kotlin {

    fun KotlinNativeTargetWithHostTests.configureCommon() {
        binaries.executable {
            entryPoint = "main"
        }
    }

    fun KotlinNativeTargetWithHostTests.configureMacOS() {
        compilations.getByName("main") {
            val current by cinterops.creating {
                defFile("src/macosMain/interop/current.def")
                packageName("current")
                includeDirs("src/macosMain/interop/include")
            }
        }
    }

    if (build_jvm) {
        jvm()
    }

    allow(Platform.MACOS) {
        macosX64 {
            configureCommon()
            configureMacOS()
        }
        macosArm64 {
            configureCommon()
            configureMacOS()
        }
    }

    allow(Platform.LINUX) {
        linuxX64 {
            configureCommon()
        }
    }

    allow(Platform.WINDOWS) {
        mingwX64 {
            configureCommon()
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.cbor)

            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.optimal)

            implementation("com.squareup.okio:okio:3.17.0-SNAPSHOT")
            implementation("com.github.ajalt.clikt:clikt:5.0.3")
            implementation("com.github.ajalt.mordant:mordant-coroutines:3.0.2")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }

        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }
}

tasks.register("release") {
    group = "build"
    description = "compile release binaries"

    allow(Platform.WINDOWS) {
        dependsOn("linkReleaseExecutableMingwX64")
    }

    allow(Platform.MACOS) {
        //only macOS target can be call xcode tools.
        if (platform == Platform.MACOS) {
            dependsOn("linkReleaseExecutableMacosX64")
            dependsOn("linkReleaseExecutableMacosArm64")
        }
    }

    allow(Platform.LINUX) {
        dependsOn("linkReleaseExecutableLinuxX64")
    }
}
