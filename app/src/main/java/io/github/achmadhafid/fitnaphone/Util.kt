package io.github.achmadhafid.fitnaphone

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

inline val Context.canRunActivityFromService
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Settings.canDrawOverlays(this)
    } else true

inline val systemAlertWindowSettingScreen
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    } else null
