package uy.quicktrip.wearqa;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.google.android.gms.wearable.Wearable;

public final class PhoneActivity extends Activity {
    private TextView access;
    private TextView watch;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView screen = new ScrollView(this);
        screen.setBackgroundColor(Color.rgb(8,17,27));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48,60,48,40);
        screen.addView(layout);
        label(layout,"QUICK TRIP · WEAR SYNC",22,0xff66ffd2);
        label(layout,"Puente QA auténtico con Mapa QT (solo lectura)",15,Color.WHITE);
        label(layout,"No controla reservas. No usa Internet, GPS ni contraseñas de Mapa QT.",13,0xffb6c7d3);
        access = label(layout,"Acceso a notificaciones: comprobando…",17,Color.WHITE);
        watch = label(layout,"Galaxy Watch: comprobando…",17,Color.WHITE);
        action(layout,"1 · Permitir acceso a notificaciones",()->{
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });
        action(layout,"2 · Sincronizar estado ahora",()->{
            if (!QtNotificationListener.requestRefresh()) {
                PhoneBridge.publish(this,"shift","Permiso pendiente","Habilitá el acceso a notificaciones de QT Sync Teléfono");
            }
            refresh();
        });
        label(layout,"El acceso te permite leer las notificaciones de Mapa QT. El programa ignora las demás aplicaciones. Podés revocar el permiso en Ajustes.",13,0xffb6c7d3);
        setContentView(screen);
    }
    private TextView label(LinearLayout root,String value,int size,int color){
        TextView t = new TextView(this);
        t.setText(value);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER);
        t.setPadding(0,8,0,22);
        root.addView(t,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return t;
    }
    private void action(LinearLayout root,String label,Runnable task){
        Button button=new Button(this);button.setText(label);button.setAllCaps(false);button.setOnClickListener(v->task.run());
        root.addView(button,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
    }
    @Override protected void onResume(){super.onResume();refresh();}
    private void refresh() {
        NotificationManager nm=getSystemService(NotificationManager.class);
        boolean granted = nm!=null && nm.isNotificationListenerAccessGranted(new ComponentName(this,QtNotificationListener.class));
        access.setText(granted?"Notificaciones: permiso concedido":"Notificaciones: permiso pendiente");
        Wearable.getNodeClient(this).getConnectedNodes()
            .addOnSuccessListener(nodes->watch.setText(nodes.isEmpty()?"Galaxy Watch: sin conexión Data Layer":"Wear OS: "+nodes.size()+" dispositivo(s) conectado(s)"))
            .addOnFailureListener(e->watch.setText("Wear OS: no se pudo comprobar conexión"));
    }
}
