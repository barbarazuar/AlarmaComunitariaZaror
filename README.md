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

Conexión con los microservicios de `backend/` mediante Retrofit y consumo de una API externa de clima. No incluye notificaciones push remotas.

## Integración con los microservicios y API externa

Capas: `Pantalla -> ViewModel -> AlarmaRepository -> (Room | Retrofit)`.

| Archivo | Función |
|---|---|
| `data/remote/ModelosRemotos.kt` | Modelos de solicitud y respuesta (coinciden con los DTO de Spring) |
| `data/remote/ApiServices.kt` | Interfaces Retrofit: `AlertasApi`, `UsuariosApi`, `ClimaApi` |
| `data/remote/RetrofitClient.kt` | URLs base y clientes. `HOST = "10.0.2.2"` es el PC visto desde el emulador |
| `data/remote/ResultadoRemoto.kt` | `Exito`, `ErrorHttp` o `SinConexion`; lee el JSON de error del backend |
| `data/repository/AlarmaRepository.kt` | Une Room con los servicios y define qué pasa sin conexión |

**Regla de datos (remoto vs Room):**

- La interfaz siempre lee de Room, así el historial funciona sin conexión.
- Crear alerta: se guarda en Room y se envía a `ms-alertas`. Sin conexión queda como *pendiente de envío* (`remoteId = null`) y se reenvía al abrir el historial o al deslizar para refrescar.
- Historial: al abrirlo se suben los pendientes y se descargan las alertas del servidor a Room.
- Marcar atendida, editar y eliminar una alerta ya enviada requieren conexión; si falla, Room no cambia y se avisa al usuario.
- Registro e inicio de sesión usan `ms-usuarios` y guardan una copia en Room. Sin conexión se acepta la cuenta guardada en el teléfono.
- Vecinos: salen de `GET /api/usuarios`; si falla se muestran los contactos de demostración.

**API externa:** [Open-Meteo](https://open-meteo.com/) (clima actual, sin clave). Se muestra en el panel como tarjeta con temperatura, viento y descripción del sector. Si no hay internet, la tarjeta indica que no está disponible.

**Ejecutar todo junto (emulador):**

1. Levantar `ms-usuarios` y `ms-alertas` (ver `backend/README.md`).
2. Ejecutar la app en el emulador. En un teléfono físico, cambiar `HOST` en `RetrofitClient.kt` por la IP del PC en la misma red WiFi.
3. La app permite HTTP sin cifrar (`usesCleartextTraffic`) solo porque los microservicios de desarrollo no usan HTTPS.

**Pruebas de repositorio:** `AlarmaRepositoryTest` usa `MockWebServer` para simular respuestas exitosas, errores 400/401/404/409/500 y falta de conexión, con DAOs en memoria.

## Trabajo con ramas

Las ramas `feature/*` se integran mediante Pull Request hacia `develop`. Cuando la versión integrada ha sido probada por el equipo, `develop` se fusiona hacia `main`.
