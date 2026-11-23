package com.example.alarmacomunitariazaror.data

import com.example.alarmacomunitariazaror.data.remote.AlertasApi
import com.example.alarmacomunitariazaror.data.remote.ClimaApi
import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.data.remote.RetrofitClient
import com.example.alarmacomunitariazaror.data.remote.UsuariosApi
import com.example.alarmacomunitariazaror.data.repository.AlarmaRepository
import com.example.alarmacomunitariazaror.data.repository.EstadoEnvio
import com.example.alarmacomunitariazaror.data.repository.ResultadoAuth
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.Usuario
import com.example.alarmacomunitariazaror.util.sha256
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Simula los microservicios con MockWebServer: Retrofit hace llamadas HTTP reales contra un
// servidor falso que responde lo que cada prueba encola (exito, error o sin conexion).
class AlarmaRepositoryTest {

    private lateinit var servidor: MockWebServer
    private lateinit var alertaDao: FakeAlertaDao
    private lateinit var usuarioDao: FakeUsuarioDao
    private lateinit var repo: AlarmaRepository

    @Before
    fun preparar() {
        servidor = MockWebServer().also { it.start() }
        alertaDao = FakeAlertaDao()
        usuarioDao = FakeUsuarioDao()
        repo = crearRepo(servidor.url("/").toString())
    }

    @After
    fun cerrar() {
        runCatching { servidor.shutdown() }
    }

    // Una URL a la que nadie escucha: la llamada falla con IOException (sin conexion)
    private fun crearRepoSinServidor() = crearRepo("http://localhost:1/")

    private fun crearRepo(base: String) = AlarmaRepository(
        alertaDao = alertaDao,
        usuarioDao = usuarioDao,
        alertasApi = RetrofitClient.crear(base, AlertasApi::class.java),
        usuariosApi = RetrofitClient.crear(base, UsuariosApi::class.java),
        climaApi = RetrofitClient.crear(base, ClimaApi::class.java),
        urlClima = base + "v1/forecast"
    )

    private fun json(codigo: Int, cuerpo: String = "") =
        MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(cuerpo)

    private fun alertaLocal(id: Int = 0, remoteId: Long? = null, estado: String = "ACTIVA") = Alerta(
        id = id, vecinoNombre = "Ana", ubicacion = "Lat -33.4, Lon -70.7", fechaHora = 1000L,
        tipoEmergencia = "Incendio", descripcion = "Humo", remoteId = remoteId, estado = estado,
        sincronizada = remoteId != null
    )

    private fun alertaJson(id: Long, estado: String = "ACTIVA", tipo: String = "Incendio") =
        """{"id":$id,"vecinoNombre":"Ana","usuarioId":null,"ubicacion":"Lat -33.4","fechaHora":1000,""" +
            """"tipoEmergencia":"$tipo","descripcion":"Humo","estado":"$estado"}"""

    // ------------------------------------------------------------------ alertas

    @Test
    fun `guardar alerta con servidor disponible queda sincronizada con el id remoto`() = runTest {
        servidor.enqueue(json(201, alertaJson(7)))

        val envio = repo.guardarAlerta(alertaLocal().copy(usuarioRemotoId = 3))

        assertEquals(EstadoEnvio.SINCRONIZADA, envio)
        val guardada = alertaDao.datos.single()
        assertEquals(7L, guardada.remoteId)
        assertTrue(guardada.sincronizada)
        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/api/alertas", peticion.path)
        val cuerpo = peticion.body.readUtf8()
        assertTrue(cuerpo.contains("\"usuarioId\":3"))
        assertTrue(cuerpo.contains("\"tipoEmergencia\":\"Incendio\""))
    }

    @Test
    fun `guardar alerta sin conexion la deja pendiente en Room`() = runTest {
        val envio = crearRepoSinServidor().guardarAlerta(alertaLocal())

        assertEquals(EstadoEnvio.SOLO_LOCAL, envio)
        val guardada = alertaDao.datos.single()
        assertNull(guardada.remoteId)
        assertFalse(guardada.sincronizada)
    }

    @Test
    fun `guardar alerta rechazada por el servidor queda solo en Room`() = runTest {
        servidor.enqueue(json(400, """{"status":400,"mensaje":"Datos invalidos","errores":{"ubicacion":"La ubicacion es obligatoria"}}"""))

        val envio = repo.guardarAlerta(alertaLocal())

        assertEquals(EstadoEnvio.SOLO_LOCAL, envio)
        assertEquals(1, alertaDao.datos.size)
        assertNull(alertaDao.datos.single().remoteId)
    }

    @Test
    fun `sincronizar pendientes envia las alertas creadas sin conexion`() = runTest {
        alertaDao.insertar(alertaLocal())
        alertaDao.insertar(alertaLocal())
        servidor.enqueue(json(201, alertaJson(10)))
        servidor.enqueue(json(201, alertaJson(11)))

        val enviadas = repo.sincronizarPendientes()

        assertEquals(2, enviadas)
        assertEquals(setOf(10L, 11L), alertaDao.datos.map { it.remoteId }.toSet())
    }

    @Test
    fun `una alerta creada sin conexion conserva el usuario al reenviarse`() = runTest {
        crearRepoSinServidor().guardarAlerta(alertaLocal().copy(usuarioRemotoId = 3))
        servidor.enqueue(json(201, alertaJson(12)))

        repo.sincronizarPendientes()

        assertTrue(servidor.takeRequest().body.readUtf8().contains("\"usuarioId\":3"))
    }

    @Test
    fun `sincronizar pendientes se detiene si no hay conexion`() = runTest {
        alertaDao.insertar(alertaLocal())
        alertaDao.insertar(alertaLocal())

        val enviadas = crearRepoSinServidor().sincronizarPendientes()

        assertEquals(0, enviadas)
        assertTrue(alertaDao.datos.all { it.remoteId == null })
    }

    @Test
    fun `refrescar alertas trae las del servidor, conserva la foto y borra las eliminadas`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 1).copy(fotoUri = "content://foto"))
        alertaDao.insertar(alertaLocal(remoteId = 2)) // ya no existe en el servidor
        servidor.enqueue(json(200, "[" + alertaJson(1, estado = "ATENDIDA") + "," + alertaJson(5) + "]"))

        val r = repo.refrescarAlertas()

        assertEquals(ResultadoRemoto.Exito(2), r)
        val porRemoto = alertaDao.datos.associateBy { it.remoteId }
        assertEquals(setOf(1L, 5L), porRemoto.keys)
        assertEquals("ATENDIDA", porRemoto.getValue(1L).estado)
        assertEquals("content://foto", porRemoto.getValue(1L).fotoUri)
        assertTrue(porRemoto.getValue(5L).sincronizada)
    }

    @Test
    fun `refrescar alertas sin conexion conserva los datos locales`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 1))

        val r = crearRepoSinServidor().refrescarAlertas()

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals(1, alertaDao.datos.size)
    }

    @Test
    fun `cambiar estado usa PATCH y actualiza Room`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 4))
        servidor.enqueue(json(200, alertaJson(4, estado = "ATENDIDA")))

        val r = repo.cambiarEstado(alertaDao.datos.single(), Alerta.ESTADO_ATENDIDA)

        assertTrue(r is ResultadoRemoto.Exito)
        assertEquals("ATENDIDA", alertaDao.datos.single().estado)
        val peticion = servidor.takeRequest()
        assertEquals("PATCH", peticion.method)
        assertEquals("/api/alertas/4/estado", peticion.path)
        assertTrue(peticion.body.readUtf8().contains("ATENDIDA"))
    }

    @Test
    fun `cambiar estado sin conexion no modifica Room`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 4))

        val r = crearRepoSinServidor().cambiarEstado(alertaDao.datos.single(), Alerta.ESTADO_ATENDIDA)

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals("ACTIVA", alertaDao.datos.single().estado)
    }

    @Test
    fun `cambiar estado de una alerta pendiente solo toca Room`() = runTest {
        alertaDao.insertar(alertaLocal())

        val r = repo.cambiarEstado(alertaDao.datos.single(), Alerta.ESTADO_ATENDIDA)

        assertTrue(r is ResultadoRemoto.Exito)
        assertEquals("ATENDIDA", alertaDao.datos.single().estado)
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun `editar alerta usa PUT y guarda la respuesta`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 4))
        servidor.enqueue(json(200, alertaJson(4, tipo = "Robo o intento de robo")))

        val r = repo.editarAlerta(alertaDao.datos.single().copy(tipoEmergencia = "Robo o intento de robo"))

        assertTrue(r is ResultadoRemoto.Exito)
        assertEquals("Robo o intento de robo", alertaDao.datos.single().tipoEmergencia)
        assertEquals("PUT", servidor.takeRequest().method)
    }

    @Test
    fun `eliminar alerta borra en el servidor y en Room`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 4))
        servidor.enqueue(json(204))

        val r = repo.eliminarAlerta(alertaDao.datos.single())

        assertTrue(r is ResultadoRemoto.Exito)
        assertTrue(alertaDao.datos.isEmpty())
        val peticion = servidor.takeRequest()
        assertEquals("DELETE", peticion.method)
        assertEquals("/api/alertas/4", peticion.path)
    }

    @Test
    fun `eliminar alerta que el servidor ya no tiene la borra de Room`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 4))
        servidor.enqueue(json(404, """{"status":404,"mensaje":"Alerta no encontrada"}"""))

        val r = repo.eliminarAlerta(alertaDao.datos.single())

        assertTrue(r is ResultadoRemoto.Exito)
        assertTrue(alertaDao.datos.isEmpty())
    }

    @Test
    fun `eliminar alerta con error del servidor la conserva en Room`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 4))
        servidor.enqueue(json(500, """{"status":500,"mensaje":"Error interno"}"""))

        val r = repo.eliminarAlerta(alertaDao.datos.single())

        assertEquals(ResultadoRemoto.ErrorHttp(500, "Error interno"), r)
        assertEquals(1, alertaDao.datos.size)
    }

    @Test
    fun `eliminar alerta sin conexion la conserva en Room`() = runTest {
        alertaDao.insertar(alertaLocal(remoteId = 4))

        val r = crearRepoSinServidor().eliminarAlerta(alertaDao.datos.single())

        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals(1, alertaDao.datos.size)
    }

    // ------------------------------------------------------------------ usuarios

    private fun usuarioJson(id: Long = 1, correo: String = "ana@demo.cl") =
        """{"id":$id,"nombre":"Ana Demo","correo":"$correo","direccion":"Los Jazmines 425"}"""

    @Test
    fun `registrar usuario en linea guarda copia en Room con el id remoto`() = runTest {
        servidor.enqueue(json(201, usuarioJson(9)))

        val r = repo.registrarUsuario("Ana Demo", " ANA@demo.cl ", "secreta1", "Los Jazmines 425")

        assertTrue(r is ResultadoAuth.Exito && r.enLinea)
        val local = usuarioDao.datos.single()
        assertEquals(9L, local.remoteId)
        assertEquals("secreta1".sha256(), local.claveHash)
        assertTrue(servidor.takeRequest().body.readUtf8().contains("\"correo\":\"ana@demo.cl\""))
    }

    @Test
    fun `registrar usuario con correo repetido devuelve CorreoDuplicado`() = runTest {
        servidor.enqueue(json(409, """{"status":409,"mensaje":"El correo ya esta registrado"}"""))

        val r = repo.registrarUsuario("Ana Demo", "ana@demo.cl", "secreta1", "Los Jazmines 425")

        assertEquals(ResultadoAuth.CorreoDuplicado, r)
        assertTrue(usuarioDao.datos.isEmpty())
    }

    @Test
    fun `registrar usuario con datos invalidos muestra el mensaje del servidor`() = runTest {
        servidor.enqueue(json(400, """{"status":400,"mensaje":"Datos invalidos","errores":{"clave":"La clave debe tener entre 6 y 60 caracteres"}}"""))

        val r = repo.registrarUsuario("Ana Demo", "ana@demo.cl", "123", "Los Jazmines 425")

        assertEquals(ResultadoAuth.Error("La clave debe tener entre 6 y 60 caracteres"), r)
    }

    @Test
    fun `registrar usuario sin conexion crea la cuenta solo en Room`() = runTest {
        val r = crearRepoSinServidor().registrarUsuario("Ana Demo", "ana@demo.cl", "secreta1", "Los Jazmines 425")

        assertTrue(r is ResultadoAuth.Exito && !r.enLinea)
        assertNull(usuarioDao.datos.single().remoteId)
    }

    @Test
    fun `registrar sin conexion un correo que ya existe en Room devuelve CorreoDuplicado`() = runTest {
        usuarioDao.insertar(Usuario(nombre = "Ana", correo = "ana@demo.cl", claveHash = "x", direccion = "Calle 123"))

        val r = crearRepoSinServidor().registrarUsuario("Ana Demo", "ana@demo.cl", "secreta1", "Los Jazmines 425")

        assertEquals(ResultadoAuth.CorreoDuplicado, r)
    }

    @Test
    fun `iniciar sesion en linea actualiza la copia local`() = runTest {
        servidor.enqueue(json(200, usuarioJson(9)))

        val r = repo.iniciarSesion("ana@demo.cl", "secreta1")

        assertTrue(r is ResultadoAuth.Exito && r.enLinea && r.usuario.remoteId == 9L)
        assertEquals("/api/usuarios/login", servidor.takeRequest().path)
    }

    @Test
    fun `iniciar sesion con clave incorrecta devuelve CredencialesInvalidas`() = runTest {
        servidor.enqueue(json(401, """{"status":401,"mensaje":"Correo o clave incorrectos"}"""))

        assertEquals(ResultadoAuth.CredencialesInvalidas, repo.iniciarSesion("ana@demo.cl", "mala"))
    }

    @Test
    fun `iniciar sesion sin conexion acepta la cuenta guardada en el telefono`() = runTest {
        usuarioDao.insertar(
            Usuario(nombre = "Ana", correo = "ana@demo.cl", claveHash = "secreta1".sha256(), direccion = "Calle 123")
        )

        val r = crearRepoSinServidor().iniciarSesion("ana@demo.cl", "secreta1")

        assertTrue(r is ResultadoAuth.Exito && !r.enLinea)
    }

    @Test
    fun `iniciar sesion sin conexion rechaza una clave distinta a la guardada`() = runTest {
        usuarioDao.insertar(
            Usuario(nombre = "Ana", correo = "ana@demo.cl", claveHash = "secreta1".sha256(), direccion = "Calle 123")
        )

        assertEquals(ResultadoAuth.CredencialesInvalidas, crearRepoSinServidor().iniciarSesion("ana@demo.cl", "otra"))
    }

    @Test
    fun `iniciar sesion sin conexion y sin cuenta local informa el problema`() = runTest {
        val r = crearRepoSinServidor().iniciarSesion("ana@demo.cl", "secreta1")

        assertTrue(r is ResultadoAuth.Error)
    }

    @Test
    fun `obtener vecinos convierte los usuarios del servidor`() = runTest {
        servidor.enqueue(json(200, "[" + usuarioJson(1) + "," + usuarioJson(2, "pedro@demo.cl") + "]"))

        val r = repo.obtenerVecinos() as ResultadoRemoto.Exito

        assertEquals(2, r.datos.size)
        assertEquals("Ana Demo", r.datos.first().nombre)
        assertEquals("Los Jazmines 425", r.datos.first().direccion)
    }

    // ------------------------------------------------------------------ API externa

    @Test
    fun `obtener clima traduce la respuesta de Open-Meteo`() = runTest {
        servidor.enqueue(json(200, """{"current":{"temperature_2m":18.5,"wind_speed_10m":7.2,"weather_code":61}}"""))

        val r = repo.obtenerClima(-33.498, -70.717) as ResultadoRemoto.Exito

        assertEquals(18.5, r.datos.temperatura, 0.0)
        assertEquals(7.2, r.datos.viento, 0.0)
        assertEquals("Lluvia", r.datos.descripcion)
        val ruta = servidor.takeRequest().path.orEmpty()
        assertTrue(ruta.startsWith("/v1/forecast"))
        assertTrue(ruta.contains("latitude=-33.498"))
        assertTrue(ruta.contains("longitude=-70.717"))
    }

    @Test
    fun `obtener clima con error del servicio devuelve ErrorHttp`() = runTest {
        servidor.enqueue(json(500))

        assertTrue(repo.obtenerClima(-33.0, -70.0) is ResultadoRemoto.ErrorHttp)
    }

    @Test
    fun `obtener clima sin datos actuales devuelve error`() = runTest {
        servidor.enqueue(json(200, "{}"))

        assertTrue(repo.obtenerClima(-33.0, -70.0) is ResultadoRemoto.ErrorHttp)
    }

    @Test
    fun `obtener clima sin conexion devuelve SinConexion`() = runTest {
        assertTrue(crearRepoSinServidor().obtenerClima(-33.0, -70.0) is ResultadoRemoto.SinConexion)
    }
}
