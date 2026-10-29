package uy.quicktrip.wearqa;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/** Only an authenticated paired app can request re-reading existing notifications. */
public final class WatchRequestService extends WearableListenerService {
    @Override public void onMessageReceived(MessageEvent event) {
        if (!"/qt/bridge/request".equals(event.getPath())) return;
        if (!QtNotificationListener.requestRefresh()) {
            PhoneBridge.publish(this, "shift", "Permiso pendiente",
                    "Abrí QT Sync Teléfono y habilitá acceso a notificaciones");
        }
    }
}
