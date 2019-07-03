@file:Suppress("WildcardImport")

package io.github.achmadhafid.fitnaphone

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
import io.github.achmadhafid.zpack.ktx.*
import io.github.achmadhafid.zpack.util.LifecycleHandler

class BlockerService : LifecycleService() {

    //region Resource Binding

    private val notificationId by intRes(R.integer.notification_id)
    private val notificationTitle by stringRes(R.string.blocker_notification_title)
    private val notificationTitleMulti by stringRes(R.string.blocker_notification_title_multi)
    private val notificationText by stringRes(R.string.blocker_notification_content)
    private val notificationChannelId by stringRes(R.string.notification_channel_id)
    private val notificationChannelName by stringRes(R.string.notification_channel_name)
    private val notificationChannelDescription by stringRes(R.string.notification_channel_description)
    private val dpSmall by dimenRes(R.dimen.small)

    //endregion
    //region Handler

    private val handler by LifecycleHandler(lifecycle)

    //endregion

    //region Lifecycle Callback

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (!isForeground) {
            //region extract app list param

            val (appList, scanInterval) = extractParam(intent) ?: run {
                stopSelf()
                return@onStartCommand Service.START_NOT_STICKY
            }

            //endregion
            createNotification()
            makeForeground(appList)
            scanForegroundApp(appList, scanInterval)
        }

        return START_STICKY
    }

    //endregion
    //region Private Helper

    /**
     * Create notification channel (required for API 26+)
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotification() {
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
    }

    /**
     * make this service as foreground service
     * @param appList list of app to be blocked
     */
    private fun makeForeground(appList: Map<String, String>) {
        //region register this service as foreground service

        val contentTitle = "${appList.size} ${when (appList.size) {
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
        val contentTextLong = appList.values
            .map {
                SpannableStringBuilder(it).apply {
                    setSpan(
                        BulletSpan(dpSmall),
                        0, length,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }
            }
            .sortedBy { it.toString() }
            .reduce { acc, spannableString -> acc.append("\n", spannableString) }
            .append("\n")
            .append(contentTextShort)

        startForeground(
            notificationId, NotificationCompat.Builder(this, notificationChannelId)
                .setContentTitle(contentTitle)
                .setContentText(contentTextShort)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(contentTextLong)
                )
                .setSmallIcon(R.drawable.ic_lock_black_24dp)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()
        )

        //endregion
    }

    /**
     * detect foreground app every specific interval
     * @param appList list of app to be blocked
     * @param scanInterval scanning interval in milliseconds
     */
    private fun scanForegroundApp(appList: Map<String, String>, scanInterval: Long) {
        handler.post(object : Runnable {
            fun rePostDelayed() = handler.postDelayed(this, scanInterval)
            override fun run() {
                if (powerManager.isInteractive) {
                    foregroundApp?.let {
                        if (appList.contains(it)) {
                            homeLauncher()
                        }
                    } ?: stopSelf()
                }
                rePostDelayed()
            }
        })
    }

    //endregion

}

//region Parameter Passing Helper

val MainActivity.isBlockerServiceRunning
    get() = isForegroundServiceRunning(BlockerService::class.java.name)

fun MainActivity.startBlockerService(
    appList: List<AppInfo>,
    scanInterval: Long = DEFAULT_SCAN_INTERVAL
) {
    if (appList.isNotEmpty()) {
        val appMap = HashMap<String, String>()
        appList.forEach {
            appMap[it.packageName] = it.name
        }
        startForegroundServiceCompat(
            Intent(this, BlockerService::class.java)
                .putExtra(PARAM_APP_LIST, appMap)
                .putExtra(PARAM_SCAN_INTERVAL, scanInterval)
        )
    }
}

private fun extractParam(intent: Intent): Pair<HashMap<String, String>, Long>? =
    intent.run {
        @Suppress("UNCHECKED_CAST")
        Pair(
            getSerializableExtra(PARAM_APP_LIST) as? HashMap<String, String> ?: return null,
            getLongExtra(PARAM_SCAN_INTERVAL, DEFAULT_SCAN_INTERVAL)
        )
    }

private const val PARAM_APP_LIST        = "app_list"
private const val PARAM_SCAN_INTERVAL   = "scan_interval"
private const val DEFAULT_SCAN_INTERVAL = 3_000L

//endregion
