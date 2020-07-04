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
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import io.github.achmadhafid.simplepref.SimplePref
import io.github.achmadhafid.simplepref.simplePref
import io.github.achmadhafid.zpack.extension.atLeastOreo
import io.github.achmadhafid.zpack.extension.dimenRes
import io.github.achmadhafid.zpack.extension.foregroundApp
import io.github.achmadhafid.zpack.extension.intRes
import io.github.achmadhafid.zpack.extension.notificationManagerCompat
import io.github.achmadhafid.zpack.extension.openHomeLauncher
import io.github.achmadhafid.zpack.extension.powerManager
import io.github.achmadhafid.zpack.extension.stringRes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlockerService : LifecycleService(), SimplePref {

    //region Preferences

    private val blockedApps by simplePref("blocked_apps") { mutableListOf<AppInfo>() }

    //endregion
    //region Resource Binding

    private val scanInterval by intRes(R.integer.scan_interval)
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
                notificationManagerCompat.createNotificationChannel(it.apply {
                    importance = NotificationManagerCompat.IMPORTANCE_HIGH
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

            val contentTitle = "${blockedApps.size} ${when (blockedApps.size) {
                1 -> notificationTitle
                else -> notificationTitleMulti
            }}"
            val contentTextShort = SpannableString(notificationText).apply {
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    0, length,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
            val contentTextLong = blockedApps
                .map { appInfo ->
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
                    delay(scanInterval.toLong())

                    if (!canRunActivityFromService) {
                        stopSelf()
                        return@launch
                    }

                    if (powerManager.isInteractive) {
                        foregroundApp?.let { packageName ->
                            if (blockedApps contains packageName)
                                openHomeLauncher()
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
