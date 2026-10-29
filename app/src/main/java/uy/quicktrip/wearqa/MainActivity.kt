package uy.quicktrip.wearqa

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.gms.wearable.Wearable

private val Mint = Color(0xFF66FFD2)
private val Soft = Color(0xFFB6C7D3)
private val Night = Color(0xFF08111B)

data class PhoneState(
    val shiftTitle: String = "Sin datos de Mapa QT",
    val shiftText: String = "Instalá QT Sync Teléfono y autorizá sus notificaciones",
    val shiftAt: Long = 0L,
    val alertTitle: String = "",
    val alertText: String = "",
    val alertAt: Long = 0L
)

class MainActivity : ComponentActivity() {
    private var snapshot by mutableStateOf(PhoneState())
    private var transport by mutableStateOf("Actualización pendiente")
    private val updates = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) { snapshot = readState() }
    }
    private var registered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        snapshot = readState()
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 7)
        }
        setContent {
            MaterialTheme {
                WearDashboard(snapshot, transport) { requestLatest() }
            }
        }
        requestLatest()
    }
    override fun onResume() {
        super.onResume()
        if (!registered) {
            ContextCompat.registerReceiver(this, updates,
                IntentFilter(SyncListenerService.ACTION_UPDATED), ContextCompat.RECEIVER_NOT_EXPORTED)
            registered = true
        }
        snapshot = readState()
    }
    override fun onPause() {
        if (registered) { unregisterReceiver(updates); registered = false }
        super.onPause()
    }
    private fun readState(): PhoneState {
        val p = getSharedPreferences("real_bridge", MODE_PRIVATE)
        return PhoneState(
            p.getString("shift_title", "Sin datos de Mapa QT") ?: "",
            p.getString("shift_text", "Instalá QT Sync Teléfono") ?: "",
            p.getLong("shift_at", 0L),
            p.getString("alert_title", "") ?: "",
            p.getString("alert_text", "") ?: "",
            p.getLong("alert_at", 0L))
    }
    private fun requestLatest() {
        transport = "Buscando Samsung…"
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) transport = "Sin conexión al teléfono"
            else {
                transport = "Solicitando estado al Samsung…"
                nodes.forEach { node ->
                    Wearable.getMessageClient(this)
                        .sendMessage(node.id, "/qt/bridge/request", ByteArray(0))
                        .addOnFailureListener { transport = "No se pudo solicitar actualización" }
                }
            }
            snapshot = readState()
        }.addOnFailureListener { transport = "No se pudo buscar el teléfono" }
    }
}

@Composable
private fun WearDashboard(state: PhoneState, transport: String, update: () -> Unit) {
    val now = System.currentTimeMillis()
    val recent = state.shiftAt > 0 && now >= state.shiftAt && now - state.shiftAt < 120_000
    Column(
        modifier = Modifier.fillMaxSize().background(Night)
            .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text("QUICK TRIP", modifier = Modifier.fillMaxWidth(), color = Mint,
            fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("MAPA QT · DATOS REALES", modifier = Modifier.fillMaxWidth(), color = Soft,
            fontSize = 10.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(if (recent) "● Estado actualizado" else "○ Sin estado reciente",
            color = if (recent) Mint else Soft, fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text(state.shiftTitle, color = Color.White, fontSize = 15.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text(state.shiftText, color = Soft, fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        if (state.alertAt > 0) {
            Spacer(Modifier.height(5.dp))
            Text("ÚLTIMO AVISO REAL", color = Mint, fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text(state.alertTitle, color = Color.White, fontSize = 13.sp)
            Text(state.alertText, color = Soft, fontSize = 11.sp)
        }
        Spacer(Modifier.height(5.dp))
        Button(onClick = update, modifier = Modifier.fillMaxWidth()) {
            Text("Actualizar", fontSize = 12.sp)
        }
        Text(transport, color = Soft, fontSize = 9.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("QA · SOLO LECTURA", color = Soft, fontSize = 9.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}
