package uy.quicktrip.wearqa;

import java.util.Locale;

/** Strict filter: only notifications from the real Mapa QT Android package. */
public final class SourceClassifier {
    public static final String MAPA_PACKAGE = "uy.com.mapatrayectos";
    public enum Kind { SHIFT, ALERT, IGNORE }

    private SourceClassifier() {}

    public static Kind classify(String packageName, String title) {
        if (!MAPA_PACKAGE.equals(packageName) || title == null) return Kind.IGNORE;
        String t = title.trim().toUpperCase(Locale.ROOT);
        if (t.startsWith("JORNADA ACTIVA") ||
                t.startsWith("JORNADA EN PAUSA") ||
                t.startsWith("YENDO A RECOGER") ||
                t.startsWith("PASAJERO A BORDO") ||
                t.startsWith("PARADA REGISTRADA") ||
                t.startsWith("MAPA TRAYECTOS · ACCESO RÁPIDO")) return Kind.SHIFT;
        if (t.startsWith("NUEVA SOLICITUD") ||
                t.startsWith("CAMBIO DE RESERVA") ||
                t.startsWith("MENSAJE DE UN PASAJERO") ||
                t.startsWith("PRÓXIMO VIAJE") ||
                t.startsWith("HORA DE SALIR")) return Kind.ALERT;
        return Kind.IGNORE;
    }

    public static String safeText(CharSequence text) {
        if (text == null) return "";
        String s = text.toString().replaceAll("[\\p{Cntrl}&&[^\\n]]", " ").trim();
        return s.length() <= 180 ? s : s.substring(0, 177) + "…";
    }
}
