// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply true
}

// Configure ktlint
allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(false) // Enforcement is enabled now that cleanup is complete
    }
}

// Hook ktlint check into the build process (only for main sources)
subprojects {
    tasks.matching { it.name == "preBuild" }.configureEach {
        dependsOn("ktlintFormat")
    }

    // Disable ktlint for test source sets
    tasks.matching {
        it.name.contains("ktlint") && (it.name.contains("Test") || it.name.contains("AndroidTest"))
    }.configureEach {
        enabled = false
    }
}
