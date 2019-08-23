package io.github.achmadhafid.fitnaphone

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.github.achmadhafid.simplepref.extension.simplePref
import io.github.achmadhafid.zpack.ktx.atLeastOreo
import io.github.achmadhafid.zpack.ktx.dimenRes
import io.github.achmadhafid.zpack.ktx.foregroundApp
import io.github.achmadhafid.zpack.ktx.intRes
import io.github.achmadhafid.zpack.ktx.longRes
import io.github.achmadhafid.zpack.ktx.notificationManager
import io.github.achmadhafid.zpack.ktx.openHomeLauncher
import io.github.achmadhafid.zpack.ktx.powerManager
import io.github.achmadhafid.zpack.ktx.stringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlockerService : LifecycleService() {

    //region Preferences

    private val blockedApps by simplePref { mutableListOf<AppInfo>() }

    //endregion
    //region Resource Binding

    private val scanInterval by longRes(R.integer.scan_interval)
    private val notificationId by intRes(R.integer.notification_id)
    private val notificationTitle by stringRes(R.string.blocker_notification_title)
    private val notificationTitleMulti by stringRes(R.string.blocker_notification_title_multi)
    private val notificationText by stringRes(R.string.blocker_notification_content)
    private val notificationChannelId by stringRes(R.string.notification_channel_id)
    private val notificationChannelName by stringRes(R.string.notification_channel_name)
    private val notificationChannelDescription by stringRes(R.string.notification_channel_description)
    private val dpSmall by dimenRes(R.dimen.small)

    //endregion

    //region Lifecycle Callback

    override fun onCreate() {
        super.onCreate()
        //region Create notification

        @TargetApi(Build.VERSION_CODES.O)
        if (atLeastOreo()) {
            NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                notificationManager.createNotificationChannel(it.apply {
                    importance  = NotificationManager.IMPORTANCE_HIGH
                    description = notificationChannelDescription
                })
            }
        }

        //endregion
    }

    @Suppress("ComplexMethod")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (!isForeground) {
            //region Make this service foreground

            val contentTitle     = "${blockedApps.size} ${when (blockedApps.size) {
                1    -> notificationTitle
                else -> notificationTitleMulti
            }}"
            val contentTextShort = SpannableString(notificationText).apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
            val contentTextLong  = blockedApps
                .map {appInfo ->
                    SpannableStringBuilder(appInfo.name).apply {
                        setSpan(
                            BulletSpan(dpSmall),
                            0, length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                .sortedBy { appName -> appName.toString() }
                .reduce { acc, spannableString -> acc.append("\n", spannableString) }
                .append("\n")
                .append(contentTextShort)
            val notification = NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.ic_lock_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(true)
                .setContentTitle(contentTitle)
                .setContentText(contentTextShort)
                .setStyle(NotificationCompat.BigTextStyle().bigText(contentTextLong))
                .build()
            startForeground(notificationId, notification)

            //endregion
            //region Scan foreground app

            lifecycleScope.launch {
                while (true) {
                    delay(scanInterval)
                    if (powerManager.isInteractive) {
                        foregroundApp?.let {packageName ->
                            if (blockedApps.contains(packageName)) {
                                openHomeLauncher()
                            }
                        } ?: stopSelf()
                    }
                }
            }

            //endregion
            isForeground = true
        }

        return START_STICKY
    }

    //endregion

    companion object {
        var isForeground = false
            private set
    }

}
