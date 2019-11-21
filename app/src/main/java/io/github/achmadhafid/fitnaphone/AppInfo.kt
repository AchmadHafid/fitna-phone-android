package io.github.achmadhafid.fitnaphone

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import io.github.achmadhafid.zpack.ktx.atLeastNougat
import io.github.achmadhafid.zpack.ktx.getAppName

data class AppInfo(
    val packageName: String,
    val name: String,
    var blocked: Boolean
) {
    constructor(
        context: Context,
        packageName: String,
        blocked: Boolean
    ) : this(
        packageName,
        context.getAppName(packageName) ?: "",
        blocked
    )

    fun serialize() = "$packageName$SEPARATOR$name$SEPARATOR$blocked"

    companion object {
        private const val SEPARATOR = "<<>>"
        
        fun deserialize(rawString: String): AppInfo = with(rawString.split(SEPARATOR)) {
            (AppInfo(this[0], this[1], this[2].toBoolean()))
        }
    }
}

fun List<AppInfo>.contains(packageName: String) =
    find { it.packageName == packageName } != null

fun List<AppInfo>.updateBlocked(appInfo: AppInfo) {
    find { it.packageName == appInfo.packageName }?.blocked = appInfo.blocked
}

fun List<AppInfo>.resetBlocked() {
    forEach { it.blocked = false }
}

fun MutableList<AppInfo>.addIfBlockedOrRemove(appInfo: AppInfo) {
    @TargetApi(Build.VERSION_CODES.N)
    when {
        appInfo.blocked -> add(appInfo)
        atLeastNougat() -> removeIf { it.packageName == appInfo.packageName }
        else -> removeAt(indexOfFirst { it.packageName == appInfo.packageName })
    }
}
