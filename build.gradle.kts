import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.gitlab.arturbosch.detekt.Detekt

buildscript {
    repositories {
        mainRepos()
    }
    dependencies {
        classpath(PluginClasspath.AGP())
        classpath(PluginClasspath.KOTLIN())
    }
}

plugins {
//    with(Plugin.BUILD_SCAN) {id(id) version version}
    with(Plugin.DEPENDENCY_CHECKER) {id(id) version version}
    with(Plugin.DETEKT) {id(id) version version}
}

allprojects {
    repositories {
        mainRepos()
        jitpack()
    }
}

detekt {
    toolVersion = Plugin.DETEKT.version
    input       = files("$projectDir")
    config      = files("$projectDir/detekt-config.yml")
    failFast    = false
    parallel    = true
}

//buildScan {
//    termsOfServiceUrl = "https://gradle.com/terms-of-service"
//    termsOfServiceAgree = "yes"
//}

tasks.register<Delete>("clean") {
    delete(buildDir)
}

tasks.withType<Detekt>().all {
    jvmTarget = "1.8"
}

subprojects {
    tasks.withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
            allWarningsAsErrors = true
        }
    }
}
