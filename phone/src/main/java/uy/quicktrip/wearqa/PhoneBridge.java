package uy.quicktrip.wearqa;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/** Data Layer is app-private and uses the same package and signing certificate on both devices. */
public final class PhoneBridge {
    private static final String TAG = "QtWearBridge";
    private PhoneBridge() {}

    public static void publish(Context context, String kind, String title, String text) {
        if (!"shift".equals(kind) && !"alert".equals(kind)) return;
        PutDataMapRequest map = PutDataMapRequest.create("/qt/bridge/" + kind);
        map.getDataMap().putString("title", SourceClassifier.safeText(title));
        map.getDataMap().putString("text", SourceClassifier.safeText(text));
        map.getDataMap().putLong("time", System.currentTimeMillis());
        // A single phone owns the record. DataClient syncs when the watch reconnects.
        PutDataRequest request = map.asPutDataRequest().setUrgent();
        Wearable.getDataClient(context).putDataItem(request)
                .addOnFailureListener(err -> Log.w(TAG, "Unable to sync " + kind, err));
    }
}
