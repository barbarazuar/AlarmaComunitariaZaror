# Alarma Comunitaria Zaror

Aplicación Android académica para registrar alertas vecinales de demostración, conservar un historial local y sincronizarlo con dos microservicios Spring Boot. Usa Kotlin, Jetpack Compose, Material 3, MVVM, Room, Retrofit y Open-Meteo.

La alarma sonora y las notificaciones son locales. No incluye push a otros teléfonos ni controla una sirena física. Usa datos ficticios para las pruebas.

## Datos del proyecto

| Dato | Detalle |
|---|---|
| Asignatura | DSY1105 Desarrollo de Aplicaciones Móviles, Evaluación Parcial 5 |
| Problema | Una junta de vecinos necesita avisar rápido una emergencia en su sector y dejar un historial de lo ocurrido |
| Solución | App Android que registra alertas, las guarda en el teléfono (Room) y las sincroniza con dos microservicios Spring Boot. Muestra el clima del sector desde una API externa |
| Tecnologías | Kotlin, Jetpack Compose (Material 3), MVVM, Room, Retrofit, Spring Boot, H2, Kotest, MockK |
| Repositorio | https://github.com/barbarazuar/AlarmaComunitariaZaror |

## Integrantes y roles

| Persona | Integrante | Responsabilidad | Dónde está su trabajo |
|---|---|---|---|
| 1 | Cristóbal Conejeros | Interfaz y navegación: validaciones, estados de carga y error, edición y eliminación de alertas, pruebas de validación | `app/.../ui`, `navigation`, `util` |
| 2 | José Valderrama | Microservicios Spring Boot: entidades, CRUD de alertas, gestión de usuarios, pruebas del backend | `backend/ms-alertas`, `backend/ms-usuarios` |
| 3 | Bárbara Zúñiga | Integración: Retrofit, repositorio con Room, manejo sin conexión, API externa de clima y sus pruebas | `app/.../data`, `viewmodel` |
| 4 | Rodrigo Vidal | Pruebas y cobertura, versión release firmada, documentación y evidencia de entrega | `docs/`, `scripts/`, `app/build.gradle.kts` |

Los aportes de cada integrante se ven en el historial de commits de GitHub y en las tareas de Trello (ver más abajo). Cada integrante defiende el proyecto de forma individual.

## Clonar y ejecutar en cualquier PC

El repositorio no contiene llaves ni contraseñas. Para ejecutarlo en otro computador solo se necesita el JDK y el SDK de Android (ver requisitos). Los microservicios no dependen de Android ni de ninguna llave.

```powershell
git clone https://github.com/barbarazuar/AlarmaComunitariaZaror.git
cd AlarmaComunitariaZarorackend
.\gradlew.bat :ms-usuarios:bootRun     # terminal 1, puerto 8081
.\gradlew.bat :ms-alertas:bootRun      # terminal 2, puerto 8082
```

Para hacerlo todo en un paso, el script `scripts/Iniciar-Todo.ps1` detecta la IP del computador, levanta los dos microservicios, espera a que respondan y compila la APK apuntando a esa IP. Con `-Instalar` la instala en un teléfono conectado por USB y con `-Firewall` (PowerShell como administrador) abre los puertos 8081 y 8082:

```powershell
.\scripts\Iniciar-Todo.ps1 -Instalar
```

El teléfono debe estar en la misma red WiFi que el computador. La APK queda en `dist/entrega/`.

Para la aplicación, abrir la carpeta raíz en Android Studio (genera `local.properties` con el SDK) y ejecutar `app`, o compilar con `.\gradlew.bat :app:assembleDebug`. Los pasos completos están en las secciones siguientes.

## Requisitos

- Android Studio y un **JDK completo 21**, incluido el ejecutable `jlink`.
- Android SDK Platform **37.0**, Build Tools compatibles y Platform Tools para ADB.
- Emulador o teléfono con Android 7.0, **API 24**, o superior.
- Internet para descargar dependencias y consultar clima.
- Usar los wrappers incluidos: Gradle **9.6.0** en el repositorio. El backend tiene su propio build.

En PowerShell, desde la raíz:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
```

Android Studio genera `local.properties` con `sdk.dir`. Ese archivo depende de cada equipo y no se sube a Git. `gradle/gradle-daemon-jvm.properties` solicita JDK 21 para evitar que se use un JRE incompleto de un editor.

## Iniciar los microservicios y sus bases

Abrir dos terminales desde la raíz del repositorio, con `JAVA_HOME` configurado en cada una:

```powershell
# Terminal 1
cd backend
.\gradlew.bat :ms-usuarios:bootRun
```

```powershell
# Terminal 2
cd backend
.\gradlew.bat :ms-alertas:bootRun
```

Esperar `Started UsuariosApplication` y `Started AlertasApplication`. H2 crea sus bases en archivo dentro de `backend/data/`; no requiere instalar otro servidor. Mantener ambos procesos abiertos. Detener con Ctrl+C cuando terminen las pruebas.

| Servicio | Puerto | URL desde el PC | Base |
|---|---:|---|---|
| ms-usuarios | 8081 | http://localhost:8081/api/usuarios | H2 usuarios-db |
| ms-alertas | 8082 | http://localhost:8082/api/alertas | H2 alertas-db |

Comprobar desde otra terminal:

```powershell
Invoke-RestMethod http://localhost:8081/api/usuarios
Invoke-RestMethod http://localhost:8082/api/alertas
```

Consultar [endpoints, ejemplos JSON y consola H2](backend/README.md). Al ejecutar un jar directamente, usar la carpeta del microservicio como directorio de trabajo para conservar la ruta relativa de H2.

## Compilar y ejecutar Android

Desde la raíz:

```powershell
.\gradlew.bat :app:assembleDebug
& "$env:ANDROID_HOME\platform-tools\adb.exe" devices
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
```

También se puede abrir el proyecto raíz en Android Studio, seleccionar `app` y ejecutar sobre un dispositivo. Si ya está instalada una APK firmada con otra llave, Android rechazará reemplazarla; desinstalar borra datos locales y debe decidirse conscientemente.

### Conectar emulador o teléfono

El host predeterminado **10.0.2.2** corresponde al PC visto desde el emulador Android estándar. En un teléfono por WiFi, reemplazar la IP de ejemplo por la del PC y recompilar:

```powershell
.\gradlew.bat :app:assembleDebug '-PbackendHost=192.168.1.20'
```

`backendHost` se incorpora a `BuildConfig.BACKEND_HOST` y configura los dos clientes, en puertos 8081 y 8082. No contiene protocolo ni puerto. Revisar firewall y que la red permita comunicación entre dispositivos.

En **Android 17 o superior**, pulsa **Permitir red local** y concede el permiso antes de usar los servicios locales. Si se deniega, la app mantiene el respaldo local y muestra el aviso para poder solicitarlo de nuevo. El permiso no es necesario en versiones anteriores de Android.

Como alternativa por USB, con depuración autorizada:

```powershell
adb reverse tcp:8081 tcp:8081
adb reverse tcp:8082 tcp:8082
.\gradlew.bat :app:assembleRelease '-PbackendHost=127.0.0.1' --no-configuration-cache
```

La variante release requiere primero configurar la firma como se explica más abajo. No se debe usar `localhost` del teléfono como si fuera el PC sin un túnel ADB.

## Funcionalidades y arquitectura

```text
Pantallas Compose -> ViewModel -> RepositorioAlarma
                                  |
                            AlarmaRepository
                              /          \
                         Room SQLite    Retrofit
                                         |   |   |
                                  usuarios alertas Open-Meteo
                                     |       |
                                    H2      H2
```

- Login y registro validados; sesión del cliente en memoria.
- Panel con selector de emergencia, descripción opcional, confirmación y bloqueo de guardados repetidos.
- Cámara y ubicación opcionales; sonido y notificación local después de guardar correctamente.
- Historial reactivo, refresco, marcar atendida y eliminar.
- Vecinos del servicio de usuarios, con contactos ficticios si la consulta falla.
- Clima del sector mediante [Open-Meteo](https://open-meteo.com/), sin clave API.

`AuthViewModel` y `AlarmaViewModel` reciben el contrato `RepositorioAlarma`. Sus fábricas conectan la implementación real con Android; las pruebas sustituyen el repositorio sin abrir Room ni usar internet.

### Reglas de datos y alcance

- El historial siempre se lee de Room. Crear una alerta primero guarda localmente y luego intenta POST al servidor.
- Sin respuesta correcta del POST, queda pendiente con `remoteId = null`; se reintenta al abrir o refrescar el historial.
- Después de enviar pendientes se descarga la lista remota y se conserva la foto local de registros existentes.
- Cambiar estado, editar o eliminar registros ya enviados requiere respuesta remota correcta. DELETE 404 permite limpiar una copia que el servidor ya no tiene.
- Registro y login usan ms-usuarios. Ante falta de conexión se usa el respaldo local; un error HTTP de credenciales no se convierte en login offline.
- Una cuenta creada offline no se registra automáticamente después en el servidor.
- Las fotos solo existen en el teléfono; no se suben al backend.
- No hay JWT, autorización por propietario, idempotencia ni sincronización periódica con WorkManager. La lista remota actual es compartida.
- HTTP sin cifrar está permitido para los servicios de desarrollo. El backend de demostración usa H2 sin contraseña y consola habilitada. No es una configuración para publicar en internet.

## Endpoints principales

| Servicio | Operaciones |
|---|---|
| Usuarios | POST `/api/usuarios/registro`, POST `/api/usuarios/login`, GET lista e individual, PUT y DELETE por id |
| Alertas | GET lista e individual, POST, PUT y DELETE por id, PATCH `/api/alertas/{id}/estado` |
| Externo | GET `https://api.open-meteo.com/v1/forecast` con latitude, longitude y current |

El contrato completo y códigos de respuesta están en [backend/README.md](backend/README.md).

## Pruebas y cobertura

```powershell
# Desde la raíz
.\gradlew.bat :app:verificarCoberturaDebug :app:lintDebug --no-configuration-cache

# Desde backend
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification
```

| Módulo | Pruebas correctas | Cobertura de líneas de lógica |
|---|---:|---:|
| Android | 94 | 99,76 % |
| ms-alertas | 17 | 94,74 % |
| ms-usuarios | 13 | 100 % |

Los tres módulos superan el umbral del 80 %. El alcance, las exclusiones y las rutas de reportes están en [docs/PRUEBAS.md](docs/PRUEBAS.md). Los porcentajes no incluyen toda la UI ni reemplazan la prueba en un dispositivo.

### Estrategia de pruebas (guía 15)

| Tipo | Herramientas | Qué prueba | Qué se simula |
|---|---|---|---|
| ViewModel | Kotest + MockK + `kotlinx-coroutines-test` | `AlarmaViewModelKotestTest`: validación, guardado, marcar atendida, editar y eliminar | El repositorio (`RepositorioAlarma`) con MockK |
| Repositorio | Kotest + MockK | `AlarmaRepositoryKotestTest`: con respuesta del servidor queda sincronizada; sin conexión queda pendiente | `AlertaDao` y `AlertasApi` con MockK |
| Repositorio y red | JUnit + MockWebServer | `AlarmaRepositoryTest`: códigos HTTP, errores del servidor y clima | El servidor HTTP |
| Lógica y validaciones | JUnit | Validadores, navegación, conversión de códigos del clima | Nada |
| Interfaz | Compose UI Test (`androidTest`) | `HistorialScreenTest`: lista vacía, tarjetas, marcar atendida y confirmación al eliminar | Los callbacks de la pantalla |
| Backend | JUnit, Mockito, `@WebMvcTest`, `@DataJpaTest` | Servicios, controladores y repositorios | Repositorio JPA en los tests de servicio |

Las pruebas unitarias se ejecutan con JUnit Platform; el motor vintage mantiene las pruebas escritas en JUnit 4. Para correr las de interfaz hay que tener un emulador o teléfono:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las pruebas evitan errores como duplicar una alerta al pulsar dos veces, perder una alerta sin conexión o aceptar una descripción inválida, y permiten cambiar el código con seguridad.

## APK release firmada

Generar una llave nueva solo si el proyecto no tiene una llave de entrega existente:

```powershell
.\scripts\Nueva-Llave.ps1
.\scripts\Preparar-Entrega.ps1 -BackendHost 10.0.2.2
```

La llave y sus credenciales se conservan en `.private/` con acceso restringido y quedan excluidas de Git. También se admiten variables de entorno `ZAROR_STORE_FILE`, `ZAROR_STORE_PASSWORD`, `ZAROR_KEY_ALIAS` y `ZAROR_KEY_PASSWORD`. Sin llave configurada, la compilación release no falla: se firma con la llave de depuración de Android y muestra un aviso, de modo que cualquiera pueda generar una APK instalable. Esa APK sirve para pruebas; la entrega oficial se genera con la llave privada. La tarea `validarFirmaRelease` exige la llave de forma explícita.

`Preparar-Entrega.ps1` ejecuta pruebas y cobertura, lint, build release, verificación con apksigner y exporta a `dist/entrega/` la APK, huella SHA-256 y reportes. Release desactiva el registro HTTP de OkHttp. Leer [firma y entrega segura](docs/FIRMA_Y_ENTREGA.md) antes de entregar la llave al docente.

## Recorrido para la demostración

1. Levantar ambos servicios y comprobar sus endpoints.
2. Instalar y abrir la APK firmada en el dispositivo de la defensa.
3. Mostrar validaciones de registro, crear una cuenta ficticia e iniciar sesión.
4. Consultar el clima; elegir emergencia, describir y confirmar una alerta.
5. Mostrar sonido y notificación; detener el sonido.
6. Abrir historial y comprobar también la alerta en GET del backend.
7. Marcar atendida y verificar el cambio; eliminar una alerta de prueba.
8. Probar cámara y ubicación con permisos concedidos y denegados.
9. Probar una alerta sin conexión y su sincronización al recuperarla.
10. Mostrar pruebas, cobertura y verificación de firma; cerrar sesión.

Registrar la ejecución en el teléfono real: la comprobación en emulador no certifica ese dispositivo. Consultar el [registro de verificación de la entrega](docs/EVIDENCIA_ENTREGA.md).

## Colaboración: GitHub y Trello

### GitHub

- Rama principal `main`; cada persona trabajó en su propia rama (`feature/backend-microservicios`, `feature/integracion-retrofit-api-externa`, `feature/ui-operaciones-alertas-cristobal`, `feature/pruebas-release-documentacion`) y se integró con Pull Request.
- Los commits usan el formato `tipo(ámbito): descripción` (por ejemplo `feat(api): integrar API externa`) y están a nombre de quien hizo el trabajo.
- La rama `integracion-final` reúne las cuatro ramas y resuelve los conflictos entre la interfaz y la capa de datos.

### Trello

Tablero con las listas **Por hacer**, **En curso** y **Finalizado**. Tarjetas por persona:

| Persona | Tarjetas |
|---|---|
| 1 | Revisar navegación y formularios, estados de carga y error, integrar operaciones de alertas, pruebas de interfaz |
| 2 | Proyecto Spring Boot, entidades y persistencia, CRUD de alertas, gestión de usuarios, pruebas del backend |
| 3 | Configurar Retrofit, conectar repositorio con microservicios, errores y datos locales, API externa, pruebas del repositorio |
| 4 | Medición de cobertura, pruebas de ViewModel y repositorio, versión release, README, firma y entrega |

Enlace del tablero: **pendiente de conectar la cuenta y crear el tablero real**.
Las listas y tarjetas anteriores son el plan de cierre; no acreditan un tablero
creado ni una planificación histórica en Trello. La URL y las capturas reales se
agregarán cuando exista acceso. No se utilizan capturas simuladas.

El [control de cierre de entrega](docs/CIERRE_ENTREGA.md) distingue las tareas
completadas de las que requieren al equipo, el teléfono o el docente.

## Ramas

`main` contiene el código integrado. Los cambios entran mediante Pull Request con revisión de otro integrante, sin reescribir la autoría de los commits.
