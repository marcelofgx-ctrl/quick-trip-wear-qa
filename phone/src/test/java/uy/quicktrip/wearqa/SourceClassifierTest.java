package uy.quicktrip.wearqa;
import org.junit.Test;
import static org.junit.Assert.*;
public class SourceClassifierTest {
 @Test public void realMapaShift() { assertEquals(SourceClassifier.Kind.SHIFT,SourceClassifier.classify("uy.com.mapatrayectos","Jornada activa")); }
 @Test public void realMapaPickup() { assertEquals(SourceClassifier.Kind.SHIFT,SourceClassifier.classify("uy.com.mapatrayectos","Yendo a recoger · Cabify")); }
 @Test public void realReserva() { assertEquals(SourceClassifier.Kind.ALERT,SourceClassifier.classify("uy.com.mapatrayectos","NUEVA SOLICITUD")); }
 @Test public void chatAndCancellation() { assertEquals(SourceClassifier.Kind.ALERT,SourceClassifier.classify("uy.com.mapatrayectos","Mensaje de un pasajero")); assertEquals(SourceClassifier.Kind.ALERT,SourceClassifier.classify("uy.com.mapatrayectos","Cambio de reserva · QT-123")); }
 @Test public void noOtherApp() { assertEquals(SourceClassifier.Kind.IGNORE,SourceClassifier.classify("com.whatsapp","NUEVA SOLICITUD")); }
 @Test public void noMonitorFalseShift() { assertEquals(SourceClassifier.Kind.IGNORE,SourceClassifier.classify("uy.com.mapatrayectos","Traslados Conductor")); }
 @Test public void noUnknown() { assertEquals(SourceClassifier.Kind.IGNORE,SourceClassifier.classify("uy.com.mapatrayectos","Notificación desconocida")); }
 @Test public void truncate() { assertTrue(SourceClassifier.safeText("x".repeat(300)).length()<=180); }
}
