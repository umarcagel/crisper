package compress.joshattic.us

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/** Builds and updates the "Crisper is compressing" progress notification. */
object CompressionNotification {
    private const val CHANNEL_ID = "crisper_compression"
    const val NOTIFICATION_ID = 1001

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
    }

    fun build(context: Context, title: String, percent: Int): Notification {
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.notification_text, percent))
            .setProgress(100, percent, false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setContentIntent(openApp)
            .build()
    }

    /** Refresh the notification text and progress while the service is already running. */
    fun update(context: Context, title: String, percent: Int) {
        try {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            manager.notify(NOTIFICATION_ID, build(context, title, percent))
        } catch (_: Exception) {
            // Missing notification permission just hides the notification; compression keeps going.
        }
    }
}

/**
 * Keeps Crisper alive while it compresses, so a video (or a batch) keeps going when
 * the screen turns off or you switch to another app.
 */
class CompressionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CompressionNotification.ensureChannel(this)
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: getString(R.string.notification_title_default)
        val percent = intent?.getIntExtra(EXTRA_PERCENT, 0) ?: 0
        val notification = CompressionNotification.build(this, title, percent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                CompressionNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(CompressionNotification.NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_PERCENT = "percent"

        fun start(context: Context, title: String, percent: Int) {
            try {
                val intent = Intent(context, CompressionService::class.java)
                    .putExtra(EXTRA_TITLE, title)
                    .putExtra(EXTRA_PERCENT, percent)
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Exception) {
                // If Android refuses to start it, compression still runs while the app is open.
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, CompressionService::class.java))
            } catch (_: Exception) {
            }
        }
    }
}
