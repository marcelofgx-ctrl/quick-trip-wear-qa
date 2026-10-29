# Instalación sin computadora
1. Entrar al repositorio privado y abrir Actions, seleccionar el run verde.
2. Descargar el artifact **quick-trip-wear-qa-v01-debug**.
3. En Mis archivos del Samsung, extraer el ZIP y localizar app-debug.apk.
4. Instalar Wear Installer 2 en el Samsung.
5. En el Galaxy Watch activar Opciones de desarrollador (Ajustes > Acerca del reloj > Información de software) y Depuración ADB e inalámbrica.
6. Con ambos dispositivos en la misma Wi-Fi, emparejar Wear Installer 2 con el reloj (código y puerto de emparejamiento).
7. Elegir Custom APK y enviar app-debug.apk al reloj.
8. Probar los botones: todo es ficticio y no se conecta a Quick Trip.
9. Desactivar depuración en el reloj.

La firma debug puede cambiar entre ejecuciones; para instalar una versión nueva puede hacer falta desinstalar la anterior.
