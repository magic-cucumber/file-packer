plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm()
    listOf(
        macosX64(),
        macosArm64(),
        linuxX64(),
        mingwX64(),
    ).forEach {
        it.binaries.executable {
            entryPoint = "main"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.serialization.cbor)

            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.optimal)

            implementation("com.squareup.okio:okio:3.16.4")
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
