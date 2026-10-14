# Alarma Comunitaria Zaror

Aplicación Android académica desarrollada con Kotlin, Jetpack Compose, MVVM y Room. Permite registrar usuarios ficticios, crear alertas de demostración, consultar el historial local y revisar una lista ficticia de vecinos.

> **DEMO ACADÉMICA:** esta versión guarda la información solamente en el dispositivo. No envía alertas a otros teléfonos ni reemplaza servicios formales de emergencia.

## Requisitos

- Android Studio y Android SDK.
- JDK incluido con Android Studio.
- Dispositivo o emulador con Android 7.0 (API 24) o superior.

## Compilar y probar

En Windows, desde la raíz del proyecto:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

La APK se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Flujo de prueba

1. Registrar una cuenta usando información ficticia.
2. Iniciar sesión con esa cuenta.
3. Seleccionar un tipo de emergencia y escribir una descripción opcional.
4. Confirmar la alerta de demostración.
5. Revisar la alerta en el historial.
6. Probar los permisos de cámara, ubicación y notificaciones solo con datos simulados.
7. Detener la alarma y cerrar sesión.

## Pantallas

- **Login:** valida las credenciales almacenadas localmente.
- **Registro:** crea una cuenta ficticia en Room.
- **Panel:** permite preparar y confirmar una alerta local.
- **Historial:** observa las alertas guardadas en el dispositivo.
- **Vecinos:** muestra contactos ficticios incluidos para la demostración.

## Alcance actual

- Persistencia local con Room.
- Navegación con Navigation Compose.
- Sonido y notificación local.
- Cámara y ubicación opcionales mediante permisos solicitados al usarlos.
- Confirmación antes de guardar una alerta y protección contra pulsaciones repetidas.

No incluye backend, Retrofit, sincronización entre dispositivos ni notificaciones push remotas.

## Trabajo con ramas

Las ramas `feature/*` se integran mediante Pull Request hacia `develop`. Cuando la versión integrada ha sido probada por el equipo, `develop` se fusiona hacia `main`.
