package uy.quicktrip.wearqa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TripLogicTest {
    @Test fun requiresActiveShiftForOffer() {
        assertEquals(TripPhase.IDLE, reduce(WatchState(), WatchAction.SIMULATE_OFFER).phase)
    }
    @Test fun acceptsAndCompletesOneDemoTrip() {
        var s = WatchState()
        listOf(WatchAction.START_SHIFT, WatchAction.SIMULATE_OFFER,
            WatchAction.ACCEPT, WatchAction.ARRIVE_PICKUP,
            WatchAction.START_RIDE, WatchAction.COMPLETE_RIDE).forEach { s = reduce(s, it) }
        assertEquals(TripPhase.IDLE, s.phase)
        assertEquals(1, s.completed)
        assertTrue(s.shiftActive)
    }
    @Test fun rejectsWithoutCounting() {
        val s = reduce(reduce(reduce(WatchState(), WatchAction.START_SHIFT),
            WatchAction.SIMULATE_OFFER), WatchAction.REJECT)
        assertEquals(TripPhase.IDLE, s.phase)
        assertEquals(0, s.completed)
    }
    @Test fun cannotCloseShiftDuringTrip() {
        var s = WatchState()
        listOf(WatchAction.START_SHIFT, WatchAction.SIMULATE_OFFER,
            WatchAction.ACCEPT).forEach { s = reduce(s, it) }
        s = reduce(s, WatchAction.STOP_SHIFT)
        assertTrue(s.shiftActive)
        assertEquals(TripPhase.TO_PICKUP, s.phase)
    }
    @Test fun closingIdleShiftWorks() {
        val s = reduce(reduce(WatchState(), WatchAction.START_SHIFT), WatchAction.STOP_SHIFT)
        assertFalse(s.shiftActive)
    }
    @Test fun invalidTransitionsHaveNoEffect() {
        val start = WatchState()
        assertEquals(start, reduce(start, WatchAction.COMPLETE_RIDE))
        assertEquals(start, reduce(start, WatchAction.ACCEPT))
    }
}
