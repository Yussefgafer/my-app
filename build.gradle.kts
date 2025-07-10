// Top-level build file where you can add configuration options common to all sub-projects/modules.
@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.google.com")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Define versions in a single place
extra.apply {
    set("kotlinVersion", "2.0.21")
    set("kspVersion", libs.versions.ksp.get())
    set("composeCompilerVersion", libs.versions.composeCompiler.get())
}