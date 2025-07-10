// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false  // Using the main compose plugin
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

// Add JetBrains Compose repository
buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

// Define versions in a single place
extra.apply {
    set("kotlinVersion", "2.0.21")
    set("kspVersion", libs.versions.ksp.get())
    set("composeCompilerVersion", libs.versions.composeCompiler.get())
}