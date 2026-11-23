# Backend: microservicios Spring Boot

Dos microservicios independientes. Cada uno tiene su propio puerto y su propia base de datos H2 guardada en archivo, en la carpeta `backend/data/`. La carpeta viene en el repo (vacía, con un `.gitkeep`) y los archivos de la base se crean solos al arrancar.

| Microservicio | Puerto | Base URL | Responsabilidad |
|---|---|---|---|
| `ms-usuarios` | 8081 | `http://localhost:8081/api/usuarios` | Registro, login y gestión de vecinos |
| `ms-alertas` | 8082 | `http://localhost:8082/api/alertas` | CRUD de alertas y cambio de estado |

Desde el **emulador de Android** no se usa `localhost`, se usa `10.0.2.2`, que apunta al PC:
`http://10.0.2.2:8081/` y `http://10.0.2.2:8082/`.

## Requisitos

- JDK 17 o superior. Sirve el que trae Android Studio.
- No hace falta instalar Gradle ni una base de datos: el proyecto trae su wrapper `gradlew` y usa H2.

## Cómo levantarlos

Desde la carpeta `backend`, en PowerShell, con una terminal por microservicio:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :ms-usuarios:bootRun
```

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :ms-alertas:bootRun
```

Cuando en la consola aparece `Started ...Application`, el microservicio ya está corriendo.
La primera vez, `ms-alertas` carga 2 alertas ficticias para que la app no parta vacía.

También se pueden levantar los dos juntos en una sola terminal:

```powershell
.\gradlew.bat :ms-usuarios:bootRun :ms-alertas:bootRun --parallel
```

## Ver la base de datos

Consola H2 en el navegador:

- Usuarios: http://localhost:8081/h2-console, con JDBC URL `jdbc:h2:file:../data/usuarios-db;AUTO_SERVER=TRUE`
- Alertas: http://localhost:8082/h2-console, con JDBC URL `jdbc:h2:file:../data/alertas-db;AUTO_SERVER=TRUE`

Usuario `sa`, sin contraseña. Para partir de cero hay que detener los microservicios y borrar los archivos `*.db` de `backend/data/`, dejando el `.gitkeep`.

## Estructura de paquetes (igual en los dos)

```
cl.zaror.<servicio>
├── controller   endpoints REST: reciben la petición y devuelven la respuesta
├── service      lógica de negocio (validaciones de reglas, hash de clave, fechas)
├── repository   acceso a datos con Spring Data JPA
├── model        entidades JPA (tablas)
├── dto          lo que entra y sale por la API (records con validaciones)
├── exception    excepciones propias + ManejadorErrores (las convierte en JSON con su código HTTP)
└── config       beans de configuración (BCrypt, datos iniciales)
```

Flujo de una petición: `Controller -> Service -> Repository -> H2`, y la respuesta vuelve como DTO.

## Endpoints de ms-usuarios (8081)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| POST | `/api/usuarios/registro` | `RegistroRequest` | 201 `UsuarioResponse` / 400 / 409 correo repetido |
| POST | `/api/usuarios/login` | `LoginRequest` | 200 `UsuarioResponse` / 401 |
| GET | `/api/usuarios` | | 200 lista |
| GET | `/api/usuarios/{id}` | | 200 / 404 |
| PUT | `/api/usuarios/{id}` | `{nombre, direccion}` | 200 / 400 / 404 |
| DELETE | `/api/usuarios/{id}` | | 204 / 404 |

```json
// POST /api/usuarios/registro
{ "nombre": "Ana Demo", "correo": "ana@demo.cl", "clave": "secreta1", "direccion": "Los Jazmines 425" }

// Respuesta 201 (nunca devuelve la clave)
{ "id": 1, "nombre": "Ana Demo", "correo": "ana@demo.cl", "direccion": "Los Jazmines 425" }

// POST /api/usuarios/login
{ "correo": "ana@demo.cl", "clave": "secreta1" }
```

La clave se guarda con **BCrypt**, nunca en texto plano. El correo se guarda en minúsculas y no se puede repetir.

## Endpoints de ms-alertas (8082)

| Método | Ruta | Body | Respuesta |
|---|---|---|---|
| GET | `/api/alertas` | | 200 lista, de la más nueva a la más antigua |
| GET | `/api/alertas?estado=ACTIVA` | | 200 lista filtrada (`ACTIVA` o `ATENDIDA`) |
| GET | `/api/alertas?usuarioId=1` | | 200 alertas de un vecino |
| GET | `/api/alertas/{id}` | | 200 / 404 |
| POST | `/api/alertas` | `AlertaRequest` | 201 / 400 |
| PUT | `/api/alertas/{id}` | `AlertaRequest` | 200 / 400 / 404 |
| PATCH | `/api/alertas/{id}/estado` | `{ "estado": "ATENDIDA" }` | 200 / 400 / 404 |
| DELETE | `/api/alertas/{id}` | | 204 / 404 |

```json
// POST /api/alertas  (fechaHora y usuarioId son opcionales)
{
  "vecinoNombre": "Ana Demo",
  "usuarioId": 1,
  "ubicacion": "Lat -33.495, Lon -70.715",
  "fechaHora": 1795445770083,
  "tipoEmergencia": "Incendio",
  "descripcion": "Humo en el pasaje"
}

// Respuesta 201
{
  "id": 3, "vecinoNombre": "Ana Demo", "usuarioId": 1,
  "ubicacion": "Lat -33.495, Lon -70.715", "fechaHora": 1795445770083,
  "tipoEmergencia": "Incendio", "descripcion": "Humo en el pasaje", "estado": "ACTIVA"
}
```

- `fechaHora` va en milisegundos desde 1970, igual que `System.currentTimeMillis()` en la app. Si no viene, el servidor pone la hora actual.
- `tipoEmergencia` es el texto de la etiqueta del enum `TipoEmergencia` de la app, por ejemplo `"Incendio"` o `"Persona sospechosa"`.
- Toda alerta nueva parte `ACTIVA`. PUT reemplaza los datos y PATCH solo cambia el estado.

## Formato de error (los dos microservicios)

```json
{ "status": 400, "mensaje": "Datos invalidos", "errores": { "correo": "El correo no es valido" } }
```

`errores` trae campo -> mensaje cuando falla una validación. En los demás casos viene vacío.

## Pruebas

```powershell
.\gradlew.bat test
```

30 pruebas entre ambos microservicios. Ver [pruebas y cobertura](../docs/PRUEBAS.md) para los resultados y el alcance medido. El reporte HTML queda en `ms-*/build/reports/tests/test/index.html`.

| Clase | Tipo | Qué se mockea |
|---|---|---|
| `UsuarioServiceTest` | unitaria (Mockito) | `UsuarioRepository` y `PasswordEncoder` |
| `UsuarioControllerTest` | capa web (`@WebMvcTest` + MockMvc) | `UsuarioService` con `@MockitoBean` |
| `AlertaServiceTest` | unitaria (Mockito) | `AlertaRepository`; el reloj se fija en 1000 |
| `AlertaControllerTest` | capa web (`@WebMvcTest` + MockMvc) | `AlertaService` con `@MockitoBean` |
| `AlertaRepositoryTest` | persistencia (`@DataJpaTest`) | nada, usa H2 en memoria |

## Probar a mano con PowerShell

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8082/api/alertas -ContentType 'application/json' -Body '{"vecinoNombre":"Ana","ubicacion":"Los Jazmines 425","tipoEmergencia":"Incendio"}'
Invoke-RestMethod http://localhost:8082/api/alertas
```

También se puede probar con Postman usando las mismas rutas.
