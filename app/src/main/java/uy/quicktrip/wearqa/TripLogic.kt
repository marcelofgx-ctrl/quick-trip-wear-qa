package uy.quicktrip.wearqa

/** Prototipo QA: no contiene llamadas de red ni integraciones. */
enum class TripPhase { IDLE, OFFER, TO_PICKUP, AT_PICKUP, IN_RIDE }

data class WatchState(
    val shiftActive: Boolean = false,
    val phase: TripPhase = TripPhase.IDLE,
    val completed: Int = 0,
    val message: String = "Modo demostración. Sin viajes reales."
)

enum class WatchAction {
    START_SHIFT, STOP_SHIFT, SIMULATE_OFFER,
    ACCEPT, REJECT, ARRIVE_PICKUP, START_RIDE, COMPLETE_RIDE
}

fun reduce(state: WatchState, action: WatchAction): WatchState = when (action) {
    WatchAction.START_SHIFT ->
        if (!state.shiftActive) state.copy(shiftActive = true, message = "Jornada de prueba iniciada") else state
    WatchAction.STOP_SHIFT ->
        if (state.shiftActive && state.phase == TripPhase.IDLE)
            state.copy(shiftActive = false, message = "Jornada de prueba cerrada")
        else state
    WatchAction.SIMULATE_OFFER ->
        if (state.shiftActive && state.phase == TripPhase.IDLE)
            state.copy(phase = TripPhase.OFFER, message = "Solicitud simulada recibida")
        else state
    WatchAction.ACCEPT ->
        if (state.shiftActive && state.phase == TripPhase.OFFER)
            state.copy(phase = TripPhase.TO_PICKUP, message = "Viaje de prueba aceptado")
        else state
    WatchAction.REJECT ->
        if (state.shiftActive && state.phase == TripPhase.OFFER)
            state.copy(phase = TripPhase.IDLE, message = "Solicitud de prueba rechazada")
        else state
    WatchAction.ARRIVE_PICKUP ->
        if (state.shiftActive && state.phase == TripPhase.TO_PICKUP)
            state.copy(phase = TripPhase.AT_PICKUP, message = "Llegada a recogida simulada")
        else state
    WatchAction.START_RIDE ->
        if (state.shiftActive && state.phase == TripPhase.AT_PICKUP)
            state.copy(phase = TripPhase.IN_RIDE, message = "Viaje de prueba iniciado")
        else state
    WatchAction.COMPLETE_RIDE ->
        if (state.shiftActive && state.phase == TripPhase.IN_RIDE)
            state.copy(phase = TripPhase.IDLE, completed = state.completed + 1, message = "Viaje de prueba finalizado")
        else state
}
