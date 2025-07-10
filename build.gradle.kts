// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}

// Define versions in a single place
extra.apply {
    set("kotlinVersion", "2.0.21")
    set("kspVersion", "2.0.21-1.0.21")
    set("composeCompilerVersion", "1.5.9")
}