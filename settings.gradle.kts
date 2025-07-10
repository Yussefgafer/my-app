pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.google.com")
    }
    
    resolutionStrategy {
        eachPlugin {
            when {
                requested.id.namespace == "com.android" -> 
                    useModule("com.android.tools.build:gradle:${requested.version}")
                requested.id.namespace == "org.jetbrains.kotlin" -> 
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                requested.id.id.startsWith("com.google.dagger.hilt") -> 
                    useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
                requested.id.id.startsWith("com.google.devtools.ksp") ->
                    useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.google.com")
    }
    
    // Version catalog is defined in gradle/libs.versions.toml and will be automatically included
    // No need to explicitly define it here as it causes duplicate definition
}

// Enable feature preview for version catalogs
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "ChateX"
include(":app")
