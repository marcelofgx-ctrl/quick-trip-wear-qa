package uy.quicktrip.wearqa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

private val Mint = Color(0xFF66FFD2)
private val Soft = Color(0xFFB6C7D3)
private val Night = Color(0xFF08111B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { WatchScreen() } }
    }
}

@Composable
fun WatchScreen() {
    var model by remember { mutableStateOf(WatchState()) }
    val send: (WatchAction) -> Unit = { model = reduce(model, it) }
    Column(
        modifier = Modifier.fillMaxSize().background(Night)
            .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text("QUICK TRIP", modifier = Modifier.fillMaxWidth(), color = Mint,
            fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("CONDUCTOR · QA", modifier = Modifier.fillMaxWidth(), color = Soft,
            fontSize = 10.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(5.dp))
        Text(if (model.shiftActive) "● Disponible" else "○ Fuera de jornada",
            modifier = Modifier.fillMaxWidth(), color = if (model.shiftActive) Mint else Soft,
            textAlign = TextAlign.Center, fontSize = 15.sp)
        Text("Viajes demo: " + model.completed, modifier = Modifier.fillMaxWidth(),
            color = Soft, textAlign = TextAlign.Center, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        when (model.phase) {
            TripPhase.IDLE -> {
                if (!model.shiftActive) {
                    ActionButton("Iniciar jornada", { send(WatchAction.START_SHIFT) })
                } else {
                    ActionButton("Simular solicitud", { send(WatchAction.SIMULATE_OFFER) })
                    ActionButton("Cerrar jornada", { send(WatchAction.STOP_SHIFT) })
                }
            }
            TripPhase.OFFER -> {
                Text("NUEVA SOLICITUD · DEMO", color = Mint, fontSize = 11.sp)
                Text("Aeropuerto de Carrasco", color = Color.White, fontSize = 14.sp)
                Text("→ Punta Carretas", color = Color.White, fontSize = 14.sp)
                Text("Recogida 7 min · Viaje 19 km", color = Soft, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Button(onClick = { send(WatchAction.ACCEPT) }, modifier = Modifier.weight(1f)) {
                        Text("Aceptar", fontSize = 11.sp)
                    }
                    Button(onClick = { send(WatchAction.REJECT) }, modifier = Modifier.weight(1f)) {
                        Text("Rechazar", fontSize = 11.sp)
                    }
                }
            }
            TripPhase.TO_PICKUP -> {
                Text("Rumbo a la recogida", color = Mint, fontSize = 14.sp)
                Text("Aeropuerto de Carrasco · 7 min demo", color = Soft, fontSize = 11.sp)
                ActionButton("Llegué al origen", { send(WatchAction.ARRIVE_PICKUP) })
            }
            TripPhase.AT_PICKUP -> {
                Text("En punto de recogida", color = Mint, fontSize = 14.sp)
                ActionButton("Iniciar viaje", { send(WatchAction.START_RIDE) })
            }
            TripPhase.IN_RIDE -> {
                Text("Pasajero a bordo · DEMO", color = Mint, fontSize = 13.sp)
                Text("Destino: Punta Carretas", color = Soft, fontSize = 12.sp)
                ActionButton("Finalizar viaje", { send(WatchAction.COMPLETE_RIDE) })
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(model.message, modifier = Modifier.fillMaxWidth(), color = Soft,
            fontSize = 10.sp, textAlign = TextAlign.Center)
        Text("SIN DATOS REALES", modifier = Modifier.fillMaxWidth(), color = Soft,
            fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(label, fontSize = 12.sp) }
}
