@file:Suppress("TooManyFunctions", "WildcardImport")

package io.github.achmadhafid.fitnaphone

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.PowerManager
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

//region System Service

inline val Context.activityManager
    get() = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
inline val Context.appOpsManager
    get() = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
inline val Context.usageStatsManager
    @SuppressLint("WrongConstant")
    get() = getSystemService("usagestats") as UsageStatsManager
inline val Context.notificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
inline val Context.powerManager: PowerManager
    get() = getSystemService(Context.POWER_SERVICE) as PowerManager

//endregion
//region Service Helper

@Suppress("DEPRECATION")
fun Context.getRunningServiceInfo(serviceClassName: String): ActivityManager.RunningServiceInfo? {
    for (serviceInfo in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClassName == serviceInfo.service.className) {
            return serviceInfo
        }
    }
    return null
}

fun Context.isForegroundServiceRunning(serviceClassName: String) =
    getRunningServiceInfo(serviceClassName)
        ?.foreground
        ?: false

val Service.isForeground: Boolean
    get() = isForegroundServiceRunning(this::class.java.name)

fun AppCompatActivity.startForegroundServiceCompat(intent: Intent) =
    ActivityCompat.startForegroundService(this, intent)

//endregion
//region Binding Helper

@MainThread
fun Context.stringRes(@StringRes id: Int) =
    lazy(LazyThreadSafetyMode.NONE) {
        resources.getString(id)
    }

@MainThread
fun Context.dimenRes(@DimenRes id: Int) =
    lazy(LazyThreadSafetyMode.NONE) {
        resources.getDimensionPixelSize(id)
    }

@MainThread
fun Context.intRes(@IntegerRes id: Int) =
    lazy(LazyThreadSafetyMode.NONE) {
        resources.getInteger(id)
    }
@MainThread
fun Context.longRes(@IntegerRes id: Int) =
    lazy(LazyThreadSafetyMode.NONE) {
        resources.getInteger(id)
            .toLong()
    }

@MainThread
fun longResC(context: Context, @IntegerRes id: Int) =
    context.longRes(id)

@MainThread
inline fun <reified V : View> AppCompatActivity.bindView(@IdRes id: Int) =
    lazy(LazyThreadSafetyMode.NONE) { findViewById<V>(id) }

@MainThread
inline fun <reified VM : ViewModel> AppCompatActivity.bindViewModel() =
    lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProviders.of(this)
            .get(VM::class.java)
    }

//endregion
//region Installed Application Query

fun Context.getInstalledApps(): List<ApplicationInfo> =
    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

fun Context.getInstalledAppsWithLaunchIntent(): List<ApplicationInfo> =
    getInstalledApps()
        .filter { it.packageName?.isNotEmpty() ?: false }
        .filter { it.name?.isNotEmpty() ?: false }
        .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }

fun Context.getAppName(packageName: String): String? {
    return try {
        packageManager.getApplicationLabel(
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        ).toString()
    } catch (ignored: PackageManager.NameNotFoundException) {
        null
    }
}

fun Context.getAppIcon(packageName: String): Drawable? {
    return try {
        packageManager.getApplicationIcon(packageName)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

//endregion
//region Miscellaneous

fun Context.resolveColor(@ColorRes @AttrRes id: Int) = with(TypedValue()) {
    if (theme.resolveAttribute(id, this, true)) {
        data
    } else {
        ContextCompat.getColor(this@resolveColor, id)
    }
}

fun Context.hasAppUsagePermission(): Boolean {
    val mode = appOpsManager.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        packageName
    )

    return if (mode == AppOpsManager.MODE_DEFAULT) {
        checkCallingOrSelfPermission(
            "android.permission.PACKAGE_USAGE_STATS"
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        mode == AppOpsManager.MODE_ALLOWED
    }
}

fun Context.detectForegroundApp(): String? {
    if (!hasAppUsagePermission()) {
        return null
    }

    val usageEvents = System.currentTimeMillis().let {
        @Suppress("MagicNumber")
        // query for the last one minute
        usageStatsManager.queryEvents(it - 600000L, it)
    }
    val event = UsageEvents.Event()
    var foregroundApp = ""
    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            foregroundApp = event.packageName
        }
    }

    return foregroundApp
}

fun Context.openDefaultLauncher() {
    startActivity(
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .setPackage(
                packageManager.queryIntentActivities(
                    Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME)
                    , PackageManager.MATCH_DEFAULT_ONLY
                )[0].activityInfo.packageName
            )
    )
}

fun Context.toastShort(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
        .show()

fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun <T> MutableLiveData<T>.notifyObserver() {
    value = value
}

//endregion
