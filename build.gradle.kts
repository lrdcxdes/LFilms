// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.8" apply false
}

abstract class VersionName : DefaultTask() {
    @TaskAction
    fun printVersionName() {
        // get versionname from app/build.gradle.kts
        val versionName = project.findProperty("android.defaultConfig.versionName") as String
        println(versionName)
    }
}

task<VersionName>("printVersionName")
