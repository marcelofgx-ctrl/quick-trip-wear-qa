package uy.quicktrip.wearqa;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/** READ-ONLY bridge: no PendingIntent execution, no booking writes, no Supabase. */
public final class QtNotificationListener extends NotificationListenerService {
    private static volatile QtNotificationListener active;
    private static final String TAG = "QtWearBridge";
    private String lastAlertKey = "";
    private long lastAlertAt = 0L;
    private String lastShift = "";
    private long lastShiftAt = 0L;

    static boolean requestRefresh() {
        QtNotificationListener self = active;
        if (self == null) return false;
        self.refresh();
        return true;
    }

    @Override public void onListenerConnected() {
        super.onListenerConnected();
        active = this;
        refresh();
    }

    @Override public void onListenerDisconnected() {
        if (active == this) active = null;
        PhoneBridge.publish(this, "shift", "Sin conexión con Mapa QT", "Volvé a habilitar acceso a notificaciones");
        super.onListenerDisconnected();
    }

    private void sendShift(String title, String text) {
        String key = title + "|" + text;
        long now = System.currentTimeMillis();
        if (key.equals(lastShift) && now - lastShiftAt < 20_000L) return;
        lastShift = key;
        lastShiftAt = now;
        PhoneBridge.publish(this, "shift", title, text);
    }

    /** Obtain the current state from the existing *real* ongoing notification. */
    void refresh() {
        StatusBarNotification[] notes;
        try { notes = getActiveNotifications(); }
        catch (RuntimeException ex) {
            Log.w(TAG, "Notifications not readable", ex);
            sendShift("Estado no disponible", "Abrí Mapa QT en el Samsung");
            return;
        }
        StatusBarNotification best = null;
        if (notes != null) for (StatusBarNotification n : notes) {
            String title = titleOf(n);
            if (SourceClassifier.classify(n.getPackageName(), title) == SourceClassifier.Kind.SHIFT &&
                    (best == null || n.getPostTime() > best.getPostTime())) best = n;
        }
        if (best != null) sendShift(titleOf(best), textOf(best));
        else sendShift("Estado no disponible", "No hay notificación de jornada de Mapa QT");
    }

    @Override public void onNotificationPosted(StatusBarNotification n) {
        String title = titleOf(n);
        SourceClassifier.Kind kind = SourceClassifier.classify(n.getPackageName(), title);
        if (kind == SourceClassifier.Kind.SHIFT) {
            sendShift(title, textOf(n));
        } else if (kind == SourceClassifier.Kind.ALERT) {
            // Filter repeated system updates from a single alert; never replay all old alerts.
            String key = n.getKey() + "|" + title + "|" + textOf(n);
            long now = System.currentTimeMillis();
            if (key.equals(lastAlertKey) && now - lastAlertAt < 10_000L) return;
            lastAlertKey = key;
            lastAlertAt = now;
            PhoneBridge.publish(this, "alert", title, textOf(n));
        }
    }

    @Override public void onNotificationRemoved(StatusBarNotification n) {
        if (SourceClassifier.classify(n.getPackageName(), titleOf(n)) ==
                SourceClassifier.Kind.SHIFT) refresh();
    }

    private static String titleOf(StatusBarNotification n) {
        if (n == null || n.getNotification() == null) return "";
        CharSequence value = n.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE);
        return SourceClassifier.safeText(value);
    }
    private static String textOf(StatusBarNotification n) {
        if (n == null || n.getNotification() == null) return "";
        CharSequence value = n.getNotification().extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (value == null) value = n.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT);
        return SourceClassifier.safeText(value);
    }
}
