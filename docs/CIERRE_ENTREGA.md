# Control de cierre de la entrega

Actualizado el 23 de noviembre de 2026. La base integrada es `main`, commit
`123f09c`: incluye la interfaz de Cristóbal, los cambios de datos/backend y los
casos nuevos de Kotest/MockK. El commit exacto de cada APK se conserva en
`dist/entrega/compilacion.txt`, junto a su host y fecha; su hash está en
`SHA256SUMS.txt`. No reutilizar la APK de la rama anterior como entrega integrada.

| Tarea | Estado comprobado | Qué falta |
|---|---|---|
| Trello | Listas y tareas por persona definidas en README | Conectar la cuenta, crear el tablero real, obtener URL y capturas |
| APK desde main | Pruebas, cobertura, lint y compilación release de la base integrada correctos | Conservar la salida completa de Preparar-Entrega.ps1 y verificar la APK distribuida |
| IP de la defensa | La validación local usa 192.168.1.9 | Se usará otro PC; obtener su IPv4 y recompilar antes de la defensa |
| Teléfono físico | No había un dispositivo físico conectado por ADB | Instalar, ejecutar el recorrido y registrar modelo, Android y evidencia |
| Docente | Llave JKS existente y procedimiento privado preparado | Acordar el canal y recibir confirmación expresa para equipo de cuatro |
| AVA individual | Código y APK pueden reunirse en paquetes locales | Cada integrante activa y realiza su propia entrega antes del plazo |

## Tablero real de cierre

Crear tres listas: **Por hacer**, **En curso** y **Finalizado**. Usar las personas
y responsabilidades del README. Cada tarjeta debe incluir responsable, criterio
de aceptación y enlace al commit o evidencia. Solo marcar Finalizado si el
resultado se comprobó. La creación actual de un tablero de cierre no demuestra
que se haya usado Trello durante las semanas anteriores.

Las capturas de Trello se añadirán a `docs/capturas/` después de crearlo. No usar
imágenes inventadas, una URL de ejemplo ni capturas de un tablero ajeno. El
porcentaje aplicable debe ser el de la pauta vigente del curso.

## Prueba de teléfono

Conectar por USB, habilitar depuración USB y autorizar el PC. Comprobar `adb devices`.
Instalar la release firmada. Si hay una instalación con firma distinta, evaluar
sus datos antes de desinstalarla. Usar cuentas y alertas ficticias.

1. Abrir sin cierre; comprobar validaciones, registro y login remoto.
2. Mostrar clima real, tipo de emergencia y confirmación antes de crear.
3. Comprobar el nuevo registro en el historial y en GET del backend.
4. Editar la descripción y comprobar PUT; marcar atendida y comprobar PATCH.
5. Cancelar una eliminación, luego confirmar sobre un registro ficticio y comprobar DELETE.
6. Probar cámara y foto, ubicación y sus permisos, notificación y audio.
7. Crear sin red, recuperar conexión y refrescar para enviar la pendiente.
8. Cerrar sesión, comprobar navegación y registrar resultado, modelo y versión Android.

## IP del PC de defensa

En el PC que ejecutará los servicios, consultar `ipconfig` y usar la IPv4 de la
interfaz que comparte red con el teléfono. Ejecutar desde `main`, con su JKS:

```powershell
.\scripts\Preparar-Entrega.ps1 -BackendHost DIRECCION_IPV4_REAL
```

Sustituir el marcador por la IP real. Mantener ambos microservicios activos en
8081 y 8082, comprobar conectividad desde el teléfono y repetir la prueba con
esa APK. No declarar 192.168.1.9 como IP de la defensa si se utilizará otro PC.

## Consulta al docente y entrega personal

Solicitar confirmación escrita de si se admite un equipo de cuatro (la pauta
adjunta describe parejas) y del canal privado para JKS, alias y credenciales.
No enviar la llave ni contraseñas por GitHub, README, Trello o una captura.
Conservar respaldo y confirmar recepción por el canal acordado.

Cada integrante debe ingresar a su propia cuenta AVA, usar la referencia final
de GitHub, completar nombre/apellido, número de equipo y aplicación/contexto,
subir los archivos que exija su enlace y comprobar el comprobante. Un ZIP
preparado localmente no constituye una entrega realizada.

El equipo indicó que el plazo continúa abierto. Confirmar la hora concreta en
AVA y no modificar la referencia entregada una vez vencido el plazo.
