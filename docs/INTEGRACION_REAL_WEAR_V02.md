# QA v0.2 — sincronización real de avisos Mapa QT → Galaxy Watch
## Arquitectura
- Android Mapa QT no se toca. Source verificado en `mapatrayectos/src/main/java/uy/com/mapatrayectos/TrackingService.java` y en `conductor/src/main/java/uy/com/traslados/conductor/ReservationMonitorService.java`.
- El bridge QA para Samsung escucha *solo* notificaciones del paquete `uy.com.mapatrayectos`. Requiere permiso manual de acceso a notificaciones.
- Notificaciones reconocidas: Jornada activa/en pausa, Yendo a recoger, Pasajero a bordo, Parada registrada, Nueva solicitud, Mensaje de pasajero, Cambio de reserva, Próximo viaje, Hora de salir.
- Datos via Play Services Wear OS Data Layer a la app del reloj (mismo `applicationId` y misma firma). No requiere servidor, token, PIN ni Supabase.
- Watch muestra el estado real con umbral de antigüedad de 2 minutos, último aviso real, refresco solicitado y notificación/vibración de alertas si se permite.
- **Nunca aceptar/rechazar desde el reloj**. No hay escrituras de reservas ni funciones conductoras nuevas.
## Instalación física desde Samsung
1. Descargar los DOS APK compilados en el MISMO workflow # de esta rama QA.
2. Si se instaló Wear v0.1, desinstalarla del reloj ANTES: la firma debug puede variar entre runners.
3. Instalar `quicktrip-phone-bridge-v02.apk` **solo en el Samsung** usando Mis archivos → Descargas; permitir instalación desde la fuente si Android lo solicita.
4. Abrir `QT Sync Teléfono QA` y tocar **Permitir acceso a notificaciones**. Activar únicamente este puente; Android avisa que un listener puede acceder a notificaciones del equipo, aunque el código filtra Mapa QT.
5. Confirmar que la aplicación Mapa QT tenga notificaciones habilitadas y esté realmente enviando alertas; no inventamos eventos.
6. Instalar `quicktrip-wear-v02.apk` **solo en el Galaxy Watch** mediante Wear Installer 2 y depuración inalámbrica. Ambos dispositivos deben estar emparejados vía Galaxy Wearable / Wear OS.
7. En el reloj permitir notificaciones de Quick Trip Wear, abrir la app y pulsar Actualizar.
8. Con Mapa QT abierto y jornada iniciada, comprobar "Jornada activa". Recibir una solicitud auténtica solo cuando ocurra; no generar reservas falsas.
9. Cerrar el acceso a depuración inalámbrica después.
## Criterios QA física (pendientes hasta verificar en Samsung)
- Conexión phone/watch (Data Layer) visible.
- Mapa QT jornada activa → misma etiqueta en Wear en menos de 30 s.
- Jornada pausada / recorrido / finalización: comparar estado en ambas pantallas.
- Nueva solicitud/cambio/recordatorio real → aviso y vibración en reloj una sola vez; no pulsar aceptar desde Wear.
- Apagar Bluetooth/Wi-Fi → etiqueta de estado obsoleto. Reestablecer → sincronizar estado.
- Sin permisos listener → "Permiso pendiente"; sin notificaciones → "Estado no disponible".
- Compilar ambos en un MISMO run y comparar digests de firma. El binario aún necesita QA física.
## Restricciones
- No tocar `marcelofgx-ctrl/traslados-android`, `main` del mismo, backend, producción ni Supabase.
- Apps QA se distribuyen juntas, NO son otra APK Conductor.
- No modificar el smartwatch ni instalar nada remoto automáticamente.
- Los avisos transferidos pueden contener texto de trayectos/clientes; la Data Layer usa servicios Google y podría usar cloud con cifrado de extremo a extremo si se rompe Bluetooth.
