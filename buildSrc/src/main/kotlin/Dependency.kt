@file:Suppress("TopLevelPropertyNaming")

import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies

//region Versions

private object Versions {

    const val CORE_KOTLIN     = "1.3.50"
    const val CORE_COROUTINES = "1.3.0-RC2"
    const val CORE_MATERIAL   = "1.1.0-alpha09"
    const val CORE_KOIN       = "2.0.1"

    const val JETPACK_CORE              = "1.2.0-alpha03"
    const val JETPACK_APP_COMPAT        = "1.1.0-rc01"
    const val JETPACK_LIFECYCLE         = "2.2.0-alpha03"
    const val JETPACK_ACTIVITY          = "1.1.0-alpha02"
    const val JETPACK_CONSTRAINT_LAYOUT = "2.0.0-beta2"
    const val JETPACK_RECYCLER_VIEW     = "1.1.0-beta03"
    const val JETPACK_COLLECTION        = "1.1.0"

    const val EXTRA_ZPACK                   = "0.6.5"
    const val EXTRA_SIMPLE_PREF             = "1.5.0"
    const val EXTRA_LOTTIE_DIALOG           = "3.2.5"
    const val EXTRA_TOOLBAR_BADGE_MENU_ITEM = "2.2.1"
    const val EXTRA_ONCE                    = "1.2.2"
    const val EXTRA_RECYCLER_VIEW_ANIMATORS = "3.0.0"
    const val EXTRA_TAP_TARGET_VIEW         = "1.12.0"

    const val TESTING_CORE        = "1.2.1-alpha02"
    const val TESTING_RULES       = "1.3.0-alpha02"
    const val TESTING_RUNNER      = "1.3.0-alpha02"
    const val TESTING_ESPRESSO    = "3.3.0-alpha01"
    const val TESTING_EXT_JUNIT   = "1.1.2-alpha02"
    const val TESTING_EXT_TRUTH   = "1.3.0-alpha01"
    const val TESTING_ROBOLECTRIC = "4.3"

}

//endregion
//region Configurations

private val IMPLEMENTATION              = hashSetOf("implementation")
private val TEST_IMPLEMENTATION         = hashSetOf("testImplementation")
private val ANDROID_TEST_IMPLEMENTATION = hashSetOf("androidTestImplementation")
private val UNIFIED_TEST_IMPLEMENTATION = TEST_IMPLEMENTATION + ANDROID_TEST_IMPLEMENTATION

//endregion

enum class Dependency(
    private val version: String,
    private vararg val configs: Pair<Set<String>, String>
) {
    //region Core

    KOTLIN_STDLIB(
        Versions.CORE_KOTLIN,
        IMPLEMENTATION to "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    ),
    KOTLIN_COROUTINES(
        Versions.CORE_COROUTINES,
        IMPLEMENTATION to "org.jetbrains.kotlinx:kotlinx-coroutines-core",
        IMPLEMENTATION to "org.jetbrains.kotlinx:kotlinx-coroutines-android"
    ),
    MATERIAL(
        Versions.CORE_MATERIAL,
        IMPLEMENTATION to "com.google.android.material:material"
    ),
    KOIN(
        Versions.CORE_KOIN,
        IMPLEMENTATION to "org.koin:koin-core",
        IMPLEMENTATION to "org.koin:koin-android",
        IMPLEMENTATION to "org.koin:koin-androidx-scope",
        IMPLEMENTATION to "org.koin:koin-androidx-viewmodel",
        TEST_IMPLEMENTATION to "org.koin:koin-test"
    ),

    //endregion
    //region Jetpack

    JETPACK_CORE(
        Versions.JETPACK_CORE,
        IMPLEMENTATION to "androidx.core:core-ktx"
    ),
    JETPACK_APP_COMPAT(
        Versions.JETPACK_APP_COMPAT,
        IMPLEMENTATION to "androidx.appcompat:appcompat"
    ),
    JETPACK_LIFECYCLE(
        Versions.JETPACK_LIFECYCLE,
        IMPLEMENTATION to "androidx.lifecycle:lifecycle-common-java8",
        IMPLEMENTATION to "androidx.lifecycle:lifecycle-livedata-ktx",
        IMPLEMENTATION to "androidx.lifecycle:lifecycle-viewmodel-ktx",
        IMPLEMENTATION to "androidx.lifecycle:lifecycle-extensions"
    ),
    JETPACK_ACTIVITY(
        Versions.JETPACK_ACTIVITY,
        IMPLEMENTATION to "androidx.activity:activity-ktx"
    ),
    JETPACK_CONSTRAINT_LAYOUT(
        Versions.JETPACK_CONSTRAINT_LAYOUT,
        IMPLEMENTATION to "androidx.constraintlayout:constraintlayout"
    ),
    JETPACK_RECYCLER_VIEW(
        Versions.JETPACK_RECYCLER_VIEW,
        IMPLEMENTATION to "androidx.recyclerview:recyclerview"
    ),
    JETPACK_COLLECTION(
        Versions.JETPACK_COLLECTION,
        IMPLEMENTATION to "androidx.collection:collection-ktx"
    ),

    //endregion
    //region Extra

    EXTRA_ZPACK(
        Versions.EXTRA_ZPACK,
        IMPLEMENTATION to "com.github.AchmadHafid:Zpack"
    ),
    EXTRA_SIMPLE_PREF(
        Versions.EXTRA_SIMPLE_PREF,
        IMPLEMENTATION to "com.github.AchmadHafid:SimplePref"
    ),
    EXTRA_LOTTIE_DIALOG(
        Versions.EXTRA_LOTTIE_DIALOG,
        IMPLEMENTATION to "com.github.AchmadHafid:LottieDialog"
    ),
    EXTRA_TOOLBAR_BADGE_MENU_ITEM(
        Versions.EXTRA_TOOLBAR_BADGE_MENU_ITEM,
        IMPLEMENTATION to "com.github.AchmadHafid:toolbar-badge-menu-item"
    ),
    EXTRA_ONCE(
        Versions.EXTRA_ONCE,
        IMPLEMENTATION to "com.jonathanfinerty.once:once"
    ),
    EXTRA_RECYCLER_VIEW_ANIMATORS(
        Versions.EXTRA_RECYCLER_VIEW_ANIMATORS,
        IMPLEMENTATION to "jp.wasabeef:recyclerview-animators"
    ),
    EXTRA_TAP_TARGET_VIEW(
        Versions.EXTRA_TAP_TARGET_VIEW,
        IMPLEMENTATION to "com.getkeepsafe.taptargetview:taptargetview"
    ),

    //endregion
    //region Testing

    TESTING_CORE(
        Versions.TESTING_CORE,
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test:core-ktx"
    ),

    TESTING_RULES(
        Versions.TESTING_RULES,
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test:rules"
    ),

    TESTING_RUNNER(
        Versions.TESTING_RUNNER,
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test:runner"
    ),
    TESTING_ESPRESSO(
        Versions.TESTING_ESPRESSO,
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.espresso:espresso-contrib",
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.espresso:espresso-intents",
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.espresso:espresso-accessibility",
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.espresso:espresso-remote",
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.espresso.idling:idling-concurrent",
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.espresso.idling:idling-net"
    ),
    TESTING_EXT_JUNIT(
        Versions.TESTING_EXT_JUNIT,
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.ext:junit-ktx"
    ),
    TESTING_EXT_TRUTH(
        Versions.TESTING_EXT_TRUTH,
        UNIFIED_TEST_IMPLEMENTATION to "androidx.test.ext:truth"
    ),
    TESTING_ROBOLECTRIC(
        Versions.TESTING_ROBOLECTRIC,
        TEST_IMPLEMENTATION to "org.robolectric:robolectric"
    );

    //endregion

    operator fun invoke(scope: DependencyHandlerScope) {
        configs.forEach {
            it.first.forEach {config ->
                scope.add(config, "${it.second}:$version")
            }
        }
    }

}

fun Project.dependsOn(vararg deps: Dependency) {
    dependencies { deps.forEach { it(this) } }
}
