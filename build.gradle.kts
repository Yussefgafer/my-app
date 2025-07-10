// Top-level build file where you can add configuration options common to all sub-projects/modules.
@file:Suppress("DSL_SCOPE_VIOLATION")

// Load the version catalog
val libs = rootProject.libs

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
    
    dependencies {
        classpath("com.android.tools.build:gradle:${libs.versions.agp.get()}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.google.com")
    }
    
    // Configure all projects to use the same Kotlin version
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

// Define versions in a single place
extra.apply {
    set("kotlinVersion", libs.versions.kotlin.get())
    set("kspVersion", libs.versions.ksp.get())
    set("composeCompilerVersion", libs.versions.composeCompiler.get())
}