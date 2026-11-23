package com.example.alarmacomunitariazaror.data

import com.example.alarmacomunitariazaror.data.remote.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CancellationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ResultadoRemotoTest {
    @Test fun `lee mensaje y prioriza detalle de validacion`() {
        val r = Response.error<Unit>(400, """{"mensaje":"Datos invalidos","errores":{"correo":"Correo invalido"}}""".toResponseBody())
        assertEquals("Correo invalido", mensajeDeError(r))
    }
    @Test fun `error con JSON roto usa mensaje generico`() {
        assertEquals("Error 502 del servidor", mensajeDeError(Response.error<Unit>(502, "<html>Error</html>".toResponseBody())))
    }
    @Test fun `204 se convierte en exito Unit`() = runTest {
        val r = llamar { Response.success<Unit>(204, null) }
        assertEquals(ResultadoRemoto.Exito(Unit), r)
    }
    @Test fun `IOException sin mensaje usa texto de respaldo`() = runTest {
        val r = llamar<Unit> { throw IOException() }
        assertTrue(r is ResultadoRemoto.SinConexion)
        assertEquals("Sin conexión con el servidor", (r as ResultadoRemoto.SinConexion).causa)
    }
    @Test fun `la cancelacion no se transforma en falta de conexion`() = runTest {
        try { llamar<Unit> { throw CancellationException("Cancelar") }; fail("Debe propagar cancelacion") }
        catch (e: CancellationException) { assertEquals("Cancelar", e.message) }
    }
}
