plugins {
    id(Plugin.ANDROID_APP.id)
    id(Plugin.KOTLIN_ANDROID.id)
    id(Plugin.KTX.id)
    id(Plugin.KAPT.id)
}

android {
    compileSdkVersion(AndroidSdk.COMPILE)
    defaultConfig {
        applicationId = "io.github.achmadhafid.fitnaphone"
        minSdkVersion(AndroidSdk.MIN)
        targetSdkVersion(AndroidSdk.TARGET)
        versionCode = 3
        versionName = "2.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        named("debug") {
            isShrinkResources = false
            isMinifyEnabled = false
        }
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    dexOptions {
        javaMaxHeapSize = "4g"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests.apply {
            isIncludeAndroidResources = true
        }
    }
    lintOptions {
        isWarningsAsErrors = true
    }
    packagingOptions {
        pickFirst("META-INF/atomicfu.kotlin_module")
        exclude("META-INF/LICENSE")
    }
    sourceSets {
        named("androidTest") {
            java.srcDir("src/uiTest/java")
        }
        named("test") {
            java.srcDir("src/uiTest/java")
        }
    }
}

dependsOn(
    Dependency.KOTLIN_STDLIB,
    Dependency.KOTLIN_COROUTINES,

    Dependency.MATERIAL,

    Dependency.JETPACK_CORE,
    Dependency.JETPACK_APP_COMPAT,
    Dependency.JETPACK_ACTIVITY,
    Dependency.JETPACK_CONSTRAINT_LAYOUT,
    Dependency.JETPACK_RECYCLER_VIEW,
    Dependency.JETPACK_COLLECTION,
    Dependency.JETPACK_LIFECYCLE,

    Dependency.EXTRA_TAP_TARGET_VIEW,
    Dependency.EXTRA_VIEW_ANIMATOR,
    Dependency.EXTRA_ONCE,
    Dependency.EXTRA_ZPACK,
    Dependency.EXTRA_SIMPLE_PREF,
    Dependency.EXTRA_LOTTIE_DIALOG,
    Dependency.EXTRA_TOOLBAR_BADGE_MENU_ITEM,
    Dependency.EXTRA_RECYCLER_VIEW_ANIMATORS,

    Dependency.TESTING_CORE,
    Dependency.TESTING_RULES,
    Dependency.TESTING_RUNNER,
    Dependency.TESTING_ESPRESSO,
    Dependency.TESTING_EXT_JUNIT,
    Dependency.TESTING_EXT_TRUTH,
    Dependency.TESTING_ROBOLECTRIC,

    Dependency.KAPT_JETPACK_ANNOTATION
)
