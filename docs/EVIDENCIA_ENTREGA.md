# Registro de verificación de la entrega

Fecha: 23 de noviembre de 2026. Rama: `feature/pruebas-release-documentacion`.
Código de la APK final: `1a85918` (los commits posteriores de documentación no
cambian su código). Este registro corresponde a la APK identificada por su hash;
una compilación posterior debe actualizarlo.

## Artefacto verificado

| Dato | Resultado |
|---|---|
| Archivo | `AlarmaComunitariaZaror-celular.apk` / `AlarmaComunitariaZaror-release.apk` (mismos bytes) |
| Tamaño | 14238288 bytes |
| Variante | Release, firmada, no depurable |
| Application ID | `com.example.alarmacomunitariazaror` |
| Versión | 1.0, versionCode 1 |
| Android mínimo / objetivo | API 24 / API 37 |
| JDK / Gradle empleados | JBR 21 / Gradle 9.6.0 |
| Host de los microservicios incorporado | `192.168.1.9`, puertos 8081 y 8082 |
| Comprobación de firma | `apksigner verify`: válida, esquema v2, un firmante |
| Clave pública del certificado | RSA 3072 bits |

SHA-256 de la APK:

```text
4f107e6718e4219ad9a86bc0693b29ff381352ab2af2c09e41b024abc56a853a
```

SHA-256 del certificado:

```text
2eb4ab3e1eae995a8b182941555e7930577ebfc9d8f0b9326ad9b461275116fc
```

Los artefactos locales, metadatos, reportes y salida de firma están en
`dist/entrega/`, excluido de Git. Las credenciales y la llave se conservan en
`.private/`; no forman parte de la documentación pública.

## Verificación automática

| Módulo | Pruebas correctas | Líneas de lógica cubiertas | Cobertura |
|---|---:|---:|---:|
| Android | 77 | 407 / 409 | 99,51 % |
| ms-alertas | 17 | 36 / 38 | 94,74 % |
| ms-usuarios | 13 | 26 / 26 | 100 % |

Las 107 pruebas terminaron sin fallos y se superó el umbral de líneas del 80 %
en cada módulo. El [alcance de la medición](PRUEBAS.md) detalla exclusiones;
no representa cobertura de toda la interfaz. Lint debug terminó sin errores.
La compilación final repitió las pruebas Android, cobertura, lint y release
después de corregir el selector; las pruebas backend no cambiaron.

## Verificación funcional en emulador

Entorno: AVD Pixel 10 Pro, Android 17/API 37. Servicios Spring Boot reales en
la computadora, H2 en archivo y datos ficticios. Se instaló la APK firmada con
`adb install -r`; el arranque en frío terminó con `Status: ok`.

| Caso | Resultado observado |
|---|---|
| Validación de login vacío | Muestra errores de formulario |
| Registro y login | Cuenta ficticia creada en ms-usuarios; login remoto HTTP 200 y panel visible |
| Permiso de red local denegado | Se conserva el aviso y funciona el respaldo local |
| Permiso de red local concedido | Registro/login llegan a ms-usuarios |
| API externa | Tarjeta con temperatura, viento y descripción de Open-Meteo |
| Selector de emergencia | Lista de seis opciones; selección de Incendio y Otro comprobada |
| Confirmación y creación | Alerta guardada desde la app y comprobada en GET de ms-alertas |
| Notificación | Permiso concedido y llamada de publicación tras guardar; falta comprobar recepción visual y sonido en el teléfono físico |
| Detención | Botón cambia a detener y vuelve al formulario al pulsarlo |
| Historial | Alerta visible con estado ACTIVA y enviada al servidor |
| Marcar atendida | UI y GET del recurso muestran ATENDIDA |
| Eliminar | Alerta ficticia desaparece del historial y del backend |
| Sin conexión | Wi-Fi y datos del emulador desactivados; alerta guardada como pendiente en Room |
| Recuperar conexión | Al refrescar el historial pasa a enviada; GET confirma la alerta en el servidor |

Las capturas locales del recorrido están en `dist/qa/`, y no contienen
credenciales de firma. Los reportes y pruebas no sustituyen la defensa individual.

## Correcciones detectadas al instalar

- Se desactivó la optimización parcial R8 que causaba un `IllegalAccessError`
  de `kotlin.sequences` al arrancar una release anterior.
- Se añadió declaración y solicitud de `ACCESS_LOCAL_NETWORK` para SDK 37;
  sin autorización de LAN, el registro podía resolverse solo en Room.
- Se reemplazó la capa táctil transparente del selector por
  `ExposedDropdownMenuBox` y `menuAnchor` de Material 3; el selector anterior
  no abría la lista durante el recorrido instalado.

## Pendiente en el teléfono de demostración

No había un teléfono físico conectado. Antes de la defensa se debe registrar
su modelo y versión Android, instalar esta misma APK y repetir el recorrido.
Comprobar especialmente cámara/foto, ubicación aproximada y precisa,
notificaciones concedidas y denegadas, audio/volumen, permisos, tamaño de texto
y conectividad con ambos servicios desde la red de la sala.

La IP incorporada debe seguir siendo la de la computadora del backend. Si cambia,
recompilar con el host correcto siguiendo [firma y entrega](FIRMA_Y_ENTREGA.md).
Sin servicios accesibles se conserva el respaldo local con las limitaciones
descritas en el README. El canal privado de entrega de la JKS y sus credenciales
todavía debe acordarse con el docente; no se ha enviado la llave.
