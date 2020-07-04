//region Versions

private const val VERSIONS_BUILD_SCAN         = "3.0"
private const val VERSIONS_DEPENDENCY_CHECKER = "0.28.0"
private const val VERSIONS_DETEKT             = "1.10.0"
private const val VERSIONS_AGP                = "4.0.0"
private const val VERSIONS_KOTLIN             = "1.3.72"

//endregion

enum class Plugin(val id: String, val version: String = "") {

    BUILD_SCAN("com.gradle.build-scan", VERSIONS_BUILD_SCAN),
    DEPENDENCY_CHECKER("com.github.ben-manes.versions", VERSIONS_DEPENDENCY_CHECKER),
    DETEKT("io.gitlab.arturbosch.detekt", VERSIONS_DETEKT),
    ANDROID_APP("com.android.application"),
    KOTLIN_ANDROID("kotlin-android"),
    KTX("kotlin-android-extensions"),
    KAPT("kotlin-kapt"),

}

enum class PluginClasspath(private val classpath: String, private val version: String) {

    AGP("com.android.tools.build:gradle", VERSIONS_AGP),
    KOTLIN("org.jetbrains.kotlin:kotlin-gradle-plugin", VERSIONS_KOTLIN);

    operator fun invoke() = "$classpath:$version"

}
