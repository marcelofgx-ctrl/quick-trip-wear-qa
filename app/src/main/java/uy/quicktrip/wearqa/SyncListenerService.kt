package uy.quicktrip.wearqa

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

/** Real notification snapshots from the paired Samsung. No backend access or booking actions. */
class SyncListenerService : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        try {
            for (event in events) {
                if (event.type != DataEvent.TYPE_CHANGED) continue
                val path = event.dataItem.uri.path ?: continue
                if (path != "/qt/bridge/shift" && path != "/qt/bridge/alert") continue
                val map = DataMapItem.fromDataItem(event.dataItem).dataMap
                val at = map.getLong("time", 0L)
                if (at <= 0L) continue
                val title = map.getString("title") ?: ""
                val body = map.getString("text") ?: ""
                val kind = if (path.endsWith("shift")) "shift" else "alert"
                val p = getSharedPreferences("real_bridge", MODE_PRIVATE)
                val prev = p.getLong("${kind}_at", 0L)
                if (at <= prev) continue
                p.edit().putString("${kind}_title", title)
                    .putString("${kind}_text", body)
                    .putLong("${kind}_at", at).apply()
                if (kind == "alert" && System.currentTimeMillis() - at < 120_000L) {
                    notifyNewAlert(title, body, at)
                }
                sendBroadcast(Intent(ACTION_UPDATED).setPackage(packageName))
            }
        } finally {
            events.release()
        }
    }

    private fun notifyNewAlert(title: String, text: String, at: Long) {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        val channel = "qt_wear_real_alerts_v02"
        nm.createNotificationChannel(NotificationChannel(
            channel, "Avisos reales de Mapa QT", NotificationManager.IMPORTANCE_HIGH
        ).apply { enableVibration(true) })
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val intent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 2, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = Notification.Builder(this, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        nm.notify((at % 1_000_000).toInt(), n)
    }

    companion object {
        const val ACTION_UPDATED = "uy.quicktrip.wearqa.REAL_SYNC_UPDATED"
    }
}
