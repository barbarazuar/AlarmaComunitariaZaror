package com.example.alarmacomunitariazaror.data

import com.example.alarmacomunitariazaror.data.remote.AlertasApi
import com.example.alarmacomunitariazaror.data.remote.ClimaApi
import com.example.alarmacomunitariazaror.data.remote.RetrofitClient
import com.example.alarmacomunitariazaror.data.remote.UsuariosApi
import com.example.alarmacomunitariazaror.data.repository.AlarmaRepository
import com.example.alarmacomunitariazaror.data.repository.EstadoEnvio
import com.example.alarmacomunitariazaror.model.Alerta
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

// La foto de una alerta viaja al servidor con PUT /api/alertas/{id}/foto y se descarga con GET.
// Aqui el servidor es un MockWebServer y la lectura/escritura de archivos se simula con lambdas.
class AlarmaRepositoryFotoTest {

    private lateinit var servidor: MockWebServer
    private lateinit var alertaDao: FakeAlertaDao
    private val bytesFoto = byteArrayOf(1, 2, 3, 4)
    private val guardadas = mutableListOf<Pair<Long, ByteArray>>()

    @Before
    fun preparar() {
        servidor = MockWebServer().also { it.start() }
        alertaDao = FakeAlertaDao()
    }

    @After
    fun cerrar() {
        runCatching { servidor.shutdown() }
    }

    private fun repo(lectura: ByteArray? = bytesFoto): AlarmaRepository {
        val base = servidor.url("/").toString()
        return AlarmaRepository(
            alertaDao = alertaDao,
            usuarioDao = FakeUsuarioDao(),
            alertasApi = RetrofitClient.crear(base, AlertasApi::class.java),
            usuariosApi = RetrofitClient.crear(base, UsuariosApi::class.java),
            climaApi = RetrofitClient.crear(base, ClimaApi::class.java),
            urlClima = base + "v1/forecast",
            leerFoto = { lectura },
            guardarFotoDescargada = { id, bytes -> guardadas.add(id to bytes); "content://fotos/remota_$id.jpg" }
        )
    }

    private fun json(codigo: Int, cuerpo: String = "") =
        MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(cuerpo)

    private fun alertaJson(id: Long, tieneFoto: Boolean) =
        """{"id":$id,"vecinoNombre":"Ana","usuarioId":null,"ubicacion":"Lat -33.4","fechaHora":1000,""" +
            """"tipoEmergencia":"Incendio","descripcion":"Humo","estado":"ACTIVA","tieneFoto":$tieneFoto}"""

    private fun alertaConFoto(remoteId: Long? = null) = Alerta(
        vecinoNombre = "Ana", ubicacion = "Lat -33.4", fechaHora = 1000L, tipoEmergencia = "Incendio",
        descripcion = "Humo", fotoUri = "content://fotos/alerta_1.jpg", remoteId = remoteId,
        sincronizada = remoteId != null
    )

    @Test
    fun `al crear una alerta con foto, despues del POST se sube la imagen con PUT`() = runTest {
        servidor.enqueue(json(201, alertaJson(7, false)))
        servidor.enqueue(MockResponse().setResponseCode(204))

        val envio = repo().guardarAlerta(alertaConFoto())

        assertEquals(EstadoEnvio.SINCRONIZADA, envio)
        assertEquals("POST", servidor.takeRequest().method)
        val subida = servidor.takeRequest()
        assertEquals("PUT", subida.method)
        assertEquals("/api/alertas/7/foto", subida.path)
        assertEquals("image/jpeg", subida.getHeader("Content-Type"))
        assertArrayEquals(bytesFoto, subida.body.readByteArray())
    }

    @Test
    fun `si la subida de la foto falla, la alerta igual queda sincronizada`() = runTest {
        servidor.enqueue(json(201, alertaJson(7, false)))
        servidor.enqueue(json(500, """{"status":500,"mensaje":"error"}"""))

        val envio = repo().guardarAlerta(alertaConFoto())

        assertEquals(EstadoEnvio.SINCRONIZADA, envio)
        assertEquals(7L, alertaDao.datos.single().remoteId)
    }

    @Test
    fun `una alerta sin foto no hace ninguna peticion extra`() = runTest {
        servidor.enqueue(json(201, alertaJson(7, false)))

        repo().guardarAlerta(alertaConFoto().copy(fotoUri = null))

        assertEquals(1, servidor.requestCount)
    }

    @Test
    fun `al refrescar descarga la foto de una alerta nueva que tiene imagen en el servidor`() = runTest {
        servidor.enqueue(json(200, "[" + alertaJson(5, true) + "]"))
        servidor.enqueue(
            MockResponse().setResponseCode(200).setHeader("Content-Type", "image/jpeg")
                .setBody(Buffer().write(bytesFoto))
        )

        repo().refrescarAlertas()

        assertEquals("/api/alertas", servidor.takeRequest().path)
        assertEquals("/api/alertas/5/foto", servidor.takeRequest().path)
        assertArrayEquals(bytesFoto, guardadas.single().second)
        val alerta = alertaDao.datos.single()
        assertNotNull(alerta.fotoUri)
        assertEquals("content://fotos/remota_5.jpg", alerta.fotoUri)
    }

    @Test
    fun `al refrescar vuelve a subir la foto local si el servidor no la tiene`() = runTest {
        alertaDao.insertar(alertaConFoto(remoteId = 5))
        servidor.enqueue(json(200, "[" + alertaJson(5, false) + "]"))
        servidor.enqueue(MockResponse().setResponseCode(204))

        repo().refrescarAlertas()

        assertEquals("/api/alertas", servidor.takeRequest().path)
        val subida = servidor.takeRequest()
        assertEquals("PUT", subida.method)
        assertEquals("/api/alertas/5/foto", subida.path)
    }

    @Test
    fun `al refrescar no descarga la foto si el telefono ya la tiene`() = runTest {
        alertaDao.insertar(alertaConFoto(remoteId = 5))
        servidor.enqueue(json(200, "[" + alertaJson(5, true) + "]"))

        repo().refrescarAlertas()

        assertEquals(1, servidor.requestCount)
        assertEquals("content://fotos/alerta_1.jpg", alertaDao.datos.single().fotoUri)
    }
}
