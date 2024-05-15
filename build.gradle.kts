plugins {
    id("com.utopia-rise.godot-kotlin-jvm") version "0.9.1-4.2.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta")
}

kotlin {
    jvmToolchain(11)
}

godot {

    // Registration Options
    registrationFileBaseDir.set(projectDir.resolve("gdj").also { it.mkdirs() })

    // Android Export Settings
    isAndroidExportEnabled.set(true)
    d8ToolPath.set(File("${System.getenv("ANDROID_SDK_ROOT")}/build-tools/33.0.2/d8"))
    androidCompileSdkDir.set(File("${System.getenv("ANDROID_SDK_ROOT")}/platforms/android-29"))

    // iOS Export Settings
    isGraalNativeImageExportEnabled.set(false)
    graalVmDirectory.set(File(System.getenv("GRAALVM_HOME") ?: ""))
    isIOSExportEnabled.set(false)

    // Windows Export Settings
    windowsDeveloperVCVarsPath.set(File(System.getenv("VC_VARS_PATH") ?: ""))
}
