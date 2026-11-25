# Pruebas y cobertura

## Android

Desde la raíz, con un JDK completo 21 y el SDK indicado en `local.properties`:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:testDebugUnitTest :app:coberturaDebug :app:verificarCoberturaDebug
.\gradlew.bat :app:lintDebug :app:assembleDebug
```

`coberturaDebug` produce `app/build/reports/jacoco/coberturaDebug/html/index.html`
y `app/build/reports/jacoco/coberturaDebug/coberturaDebug.xml`.
`verificarCoberturaDebug` falla si las líneas cubiertas de la lógica no llegan al 80 %.
El reporte se genera antes de verificar el umbral, incluso si todavía faltan casos.

Se incluyen ambos ViewModel, AlarmaRepository y sus conversiones, la traducción de
resultados HTTP, todas las utilidades, la interpretación del clima y los métodos
que deciden si los formularios tienen errores. Se excluyen UI Compose, DTO sin
lógica, código generado por Room y las fábricas de los companion object que abren
dependencias Android. El filtro de clases está declarado en `app/build.gradle.kts`
para el compilador Kotlin integrado de AGP 9. Si cambia AGP, revisar ese directorio;
el reporte comprueba que exista código medible para evitar reportes vacíos.

La métrica exigida es LINE y corresponde a la variante debug. El reporte también
muestra ramas e instrucciones; 80 % de líneas no significa 80 % de todas las ramas
ni demuestra por sí solo que la APK funciona en un teléfono.

## Backend

```powershell
cd backend
.\gradlew.bat test jacocoTestReport jacocoTestCoverageVerification
```

Cada servicio genera `build/reports/tests/test/index.html` y
`build/reports/jacoco/test/html/index.html`, además del XML.
La comprobación exige 80 % de líneas en los paquetes `service` de cada microservicio,
por separado. Controladores y persistencia se prueban además con MockMvc y H2.
Sus resultados no se mezclan con el porcentaje Android.

## Evidencia de entrega

Medición del 23 de noviembre de 2026, después de agregar la foto de las alertas:

| Módulo | Pruebas correctas | Líneas cubiertas de lógica | Cobertura |
|---|---:|---:|---:|
| Android | 100 | 433 de 435 | 99,54 % |
| ms-alertas | 27 | 50 de 52 | 96,15 % |
| ms-usuarios | 13 | 26 de 26 | 100 % |

Android usa `FakeRepositorio` y un dispatcher de pruebas para los ViewModel;
`MockWebServer` y DAOs en memoria para Repository; y casos de límites para
validadores, fechas, hashes y códigos de clima. La fábrica Android se conserva
en producción y recibe la Application mediante `CreationExtras`.
El backend prueba servicios aislados con Mockito, endpoints con MockMvc y
persistencia de alertas con H2 en memoria. Después de integrar la interfaz y los
casos Kotest/MockK de `main` y la foto de las alertas, son 140 pruebas entre los tres módulos, sin fallos ni
casos omitidos. Esta cifra no incluye pruebas instrumentadas: `HistorialScreenTest`
requiere ejecutar `:app:connectedDebugAndroidTest` en un dispositivo o emulador.
Lint debug terminó sin errores; las advertencias preexistentes se conservan
en el reporte y no equivalen a fallos de pruebas.

Conservar los reportes, el commit probado y la salida de los comandos. No publicar
claves de firma, contraseñas, bases H2 con datos personales ni archivos locales.
La instalación y la prueba manual de la APK release se documentan por separado.
