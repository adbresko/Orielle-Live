// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android.plugin) apply false
    alias(libs.plugins.ksp) apply false // Use the alias from libs.versions.toml
    alias(libs.plugins.google.services) apply false
}