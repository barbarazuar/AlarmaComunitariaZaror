# Firma y entrega de la APK

## Preparación

Usar un JDK completo 21, Android SDK 37 y las herramientas Build Tools del SDK.
La versión Gradle del repositorio es 9.6.0. El JBR de Android Studio incluye
las herramientas del JDK necesarias; un JRE sin `jlink` no basta para compilar.

Desde la raíz, en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
# Solo si no existe una llave de entrega previa:
.\scripts\Nueva-Llave.ps1
.\scripts\Preparar-Entrega.ps1 -BackendHost 10.0.2.2
```

Para un teléfono físico, sustituir `10.0.2.2` por la IPv4 LAN de la computadora
que ejecuta ambos microservicios. Conectar teléfono y computadora a la misma
red y permitir sus puertos 8081 y 8082 en la red privada del equipo servidor.
El host se incorpora a `BuildConfig`; cambiarlo requiere volver a compilar.
En Android 17, conceder también el permiso de red local desde el aviso de la app.
Open-Meteo necesita acceso a internet y se consulta por separado.

El backend se inicia siguiendo el [README](../README.md). Cada servicio mantiene
su propia base H2; no se requiere instalar un servidor de base de datos aparte.
La APK contiene la aplicación Android, no los procesos Spring Boot.

## Llave y credenciales

`Nueva-Llave.ps1` genera una JKS RSA de 3072 bits, válida por 10000 días,
con contraseñas aleatorias y permisos de carpeta restringidos al usuario actual.
Se niega a reemplazar una llave o configuración existente.

Los archivos privados son:

- `.private/alarma-zaror-release.jks`: almacén que contiene la clave privada.
- `.private/signing.properties`: ruta, alias y credenciales para el build local.

También se admiten `ZAROR_STORE_FILE`, `ZAROR_STORE_PASSWORD`, `ZAROR_KEY_ALIAS`
y `ZAROR_KEY_PASSWORD` como variables de entorno. No registrar sus valores en
terminales compartidos ni capturas. `validarFirmaRelease` impide generar una
entrega sin los cuatro datos y el almacén existente.

`.private/`, `dist/`, las JKS y otros almacenes de claves están excluidos de Git.
Conservar una copia privada de respaldo de la llave y sus credenciales: las
actualizaciones de una instalación deben conservar una firma compatible.

## Qué ejecuta el script de entrega

1. Pruebas Android y verificación del mínimo de cobertura de lógica del 80 %.
2. Lint debug y compilación release firmada, con configuración caché desactivada.
3. Pruebas y verificación de cobertura de ambos microservicios.
4. `apksigner verify --verbose --print-certs` y huella SHA-256 de la APK.
5. Copia de APK, reportes y metadatos de compilación a `dist/entrega/`.

La variante release no es depurable y desactiva el registro HTTP de OkHttp.
La optimización R8 está desactivada: una configuración parcial heredada produjo
`IllegalAccessError` al abrir la APK instalada. La solución prioriza una entrega
que arranque correctamente; reactivar optimización exige repetir la prueba instalada.

Comprobación manual adicional, ajustando la versión instalada de Build Tools:

```powershell
& "$env:ANDROID_HOME\build-tools\37.0.0\apksigner.bat" verify --verbose --print-certs .\dist\entrega\AlarmaComunitariaZaror-release.apk
Get-FileHash .\dist\entrega\AlarmaComunitariaZaror-release.apk -Algorithm SHA256
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r .\dist\entrega\AlarmaComunitariaZaror-release.apk
```

La huella de la APK y la del certificado representan cosas diferentes. La primera
identifica el archivo completo; la segunda identifica el certificado de firma.
La firma válida no reemplaza la instalación y el recorrido funcional.
Si Android rechaza una actualización por firma incompatible con una instalación
debug previa, evaluar sus datos antes de desinstalar: se borran Room y fotos locales.

## Evidencia y aceptación

Conservar el hash, el commit del código, los reportes de pruebas/cobertura, la
salida de firma y el registro de instalación. El [registro de entrega](EVIDENCIA_ENTREGA.md)
distingue resultados automáticos, pruebas en emulador y pruebas pendientes en el
teléfono físico. Ejecutar allí el recorrido del README antes de la defensa,
incluyendo permisos, cámara, ubicación, notificación y funcionamiento sin conexión.

## Entrega de la llave al docente

La pauta solicita la llave. Acordar con el docente un canal privado autorizado
para la JKS y entregar alias y contraseñas por un canal separado. Confirmar la
recepción y conservar el respaldo propio. No incluir secretos en commits, README,
capturas, APK ni un paquete público de entrega.

La carpeta `dist/entrega/` no contiene la JKS ni `signing.properties`. Revisar
el contenido antes de compartir cualquier ZIP. El procedimiento está preparado;
el canal concreto y la recepción por el docente todavía deben acordarse.

Referencias oficiales: [firma de aplicaciones](https://developer.android.com/studio/publish/app-signing),
[compilación desde terminal](https://developer.android.com/build/building-cmdline)
y [permiso de red local](https://developer.android.com/privacy-and-security/local-network-permission).
