package com.example.alarmacomunitariazaror.viewmodel

import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.data.repository.EstadoEnvio
import com.example.alarmacomunitariazaror.data.repository.VecinosFicticios
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.TipoEmergencia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmaViewModelTest {
    @get:Rule val main = MainDispatcherRule()
    private lateinit var repo: FakeRepositorio
    private lateinit var vm: AlarmaViewModel
    @Before fun prepararDependencias() { repo = FakeRepositorio(); vm = AlarmaViewModel(repo) }
    private fun alerta() = Alerta(vecinoNombre = "Ana", ubicacion = "Calle 123", fechaHora = 1000, tipoEmergencia = "Incendio", descripcion = "Humo")
    private fun preparar() { vm.onTipoChange(TipoEmergencia.entries.first()); vm.onDescripcionChange(" Humo en el pasaje ") }

    @Test fun `sin tipo o con descripcion invalida no guarda`() = runTest {
        val resultados = mutableListOf<Boolean>()
        vm.activarAlarma("Ana", onResultado = resultados::add)
        assertNotNull(vm.panel.value.errores.tipo)
        vm.onTipoChange(TipoEmergencia.entries.first()); vm.onDescripcionChange("abc")
        vm.activarAlarma("Ana", onResultado = resultados::add)
        assertNotNull(vm.panel.value.errores.descripcion)
        assertEquals(listOf(false, false), resultados); assertTrue(repo.guardadas.isEmpty())
        vm.onDescripcionChange("Una descripcion"); assertNull(vm.panel.value.errores.descripcion)
    }
    @Test fun `activar espera guardado evita duplicados y conserva ubicacion al detener`() = runTest {
        preparar(); repo.demora = 100; vm.onUbicacionObtenida("Sector Zaror"); vm.onFotoCapturada(null)
        val resultados = mutableListOf<Boolean>()
        val antes = System.currentTimeMillis()
        vm.activarAlarma("Ana", 4, resultados::add)
        assertTrue(vm.panel.value.guardando); assertTrue(resultados.isEmpty())
        vm.activarAlarma("Ana", onResultado = resultados::add)
        runCurrent(); assertEquals(1, repo.guardadas.size)
        advanceTimeBy(100); runCurrent()
        assertTrue(vm.panel.value.alarmaActiva); assertTrue(vm.panel.value.enviadaAlServidor)
        assertTrue(vm.panel.value.mensajeExito); assertFalse(vm.panel.value.guardando)
        val guardada = repo.guardadas.single()
        assertEquals(4L, guardada.usuarioRemotoId); assertEquals("Humo en el pasaje", guardada.descripcion)
        assertEquals("Sector Zaror", guardada.ubicacion); assertTrue(guardada.fechaHora >= antes)
        vm.activarAlarma("Ana", onResultado = resultados::add)
        assertEquals(listOf(false, true, false), resultados)
        advanceUntilIdle(); assertFalse(vm.panel.value.mensajeExito)
        vm.desactivarAlarma(); assertFalse(vm.panel.value.alarmaActiva)
        assertNull(vm.panel.value.tipo); assertEquals("Sector Zaror", vm.panel.value.ubicacion)
    }
    @Test fun `guardado local activa alarma sin afirmar sincronizacion`() = runTest {
        preparar(); repo.envio = EstadoEnvio.SOLO_LOCAL
        var resultado: Boolean? = null
        vm.activarAlarma("Ana") { resultado = it }; advanceUntilIdle()
        assertEquals(true, resultado); assertTrue(vm.panel.value.alarmaActiva)
        assertFalse(vm.panel.value.enviadaAlServidor)
    }
    @Test fun `fallo de disco informa error y permite reintentar`() = runTest {
        preparar(); repo.errorGuardar = IllegalStateException("Disco lleno")
        var resultado: Boolean? = null
        vm.activarAlarma("Ana") { resultado = it }; advanceUntilIdle()
        assertEquals(false, resultado); assertFalse(vm.panel.value.alarmaActiva)
        assertFalse(vm.panel.value.guardando); assertNotNull(vm.panel.value.mensajeError)
        vm.onTipoChange(TipoEmergencia.entries.first()); assertNull(vm.panel.value.mensajeError)
        repo.errorGuardar = null
        vm.activarAlarma("Ana") { resultado = it }; advanceUntilIdle(); assertEquals(true, resultado)
    }
    @Test fun `historial observa los cambios del repositorio mientras hay suscriptor`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.alertas.collect {} }
        runCurrent(); repo.alertas.value = listOf(alerta()); runCurrent()
        assertEquals(listOf(alerta()), vm.alertas.value)
    }
    @Test fun `refresco evita solicitudes repetidas y comunica los tres resultados`() = runTest {
        repo.demora = 100; vm.refrescarAlertas(); vm.refrescarAlertas()
        assertTrue(vm.remoto.value.refrescando); advanceUntilIdle()
        assertEquals(1, repo.refrescos); assertTrue(vm.remoto.value.mensaje!!.contains("2"))
        assertFalse(vm.remoto.value.refrescando)
        repo.refresco = ResultadoRemoto.ErrorHttp(500, "Error demo")
        vm.refrescarAlertas(); advanceUntilIdle(); assertTrue(vm.remoto.value.mensaje!!.contains("Error demo"))
        repo.refresco = ResultadoRemoto.SinConexion()
        vm.refrescarAlertas(); advanceUntilIdle(); assertTrue(vm.remoto.value.mensaje!!.contains("Sin conexión"))
        vm.mensajeRemotoMostrado(); assertNull(vm.remoto.value.mensaje)
    }
    @Test fun `operaciones pasan la alerta y muestran exito o fallo`() = runTest {
        val a = alerta()
        vm.marcarAtendida(a); advanceUntilIdle()
        assertEquals(Alerta.ESTADO_ATENDIDA, repo.estado); assertEquals(a, repo.ultimaOperacion)
        assertTrue(vm.remoto.value.mensaje!!.contains("atendida"))
        vm.editarDescripcion(a, "  Texto editado  "); advanceUntilIdle()
        assertEquals("Texto editado", repo.ultimaOperacion!!.descripcion)
        assertTrue(vm.remoto.value.mensaje!!.contains("actualizada"))
        vm.eliminarAlerta(a); advanceUntilIdle(); assertTrue(vm.remoto.value.mensaje!!.contains("eliminada"))
        repo.resultadoOperacion = ResultadoRemoto.ErrorHttp(404, "No existe")
        vm.eliminarAlerta(a); advanceUntilIdle(); assertTrue(vm.remoto.value.mensaje!!.contains("No existe"))
        repo.resultadoOperacion = ResultadoRemoto.SinConexion()
        vm.marcarAtendida(a); advanceUntilIdle(); assertTrue(vm.remoto.value.mensaje!!.contains("Sin conexión"))
    }
    @Test fun `clima usa coordenadas y bloquea consultas simultaneas`() = runTest {
        repo.demora = 100; vm.cargarClima(-33.5, -70.7); vm.cargarClima()
        assertTrue(vm.clima.value.cargando); advanceUntilIdle()
        assertEquals(1, repo.consultasClima); assertEquals(-33.5 to -70.7, repo.coordenadas)
        assertEquals(20.0, vm.clima.value.clima!!.temperatura, 0.0); assertFalse(vm.clima.value.cargando)
        repo.resultadoClima = ResultadoRemoto.ErrorHttp(500, "Error")
        vm.cargarClima(); advanceUntilIdle(); assertNotNull(vm.clima.value.error)
        repo.resultadoClima = ResultadoRemoto.SinConexion()
        vm.cargarClima(); advanceUntilIdle(); assertTrue(vm.clima.value.error!!.contains("sin conexión"))
    }
    @Test fun `vecinos distingue datos remotos de contactos de demostracion`() = runTest {
        vm.cargarVecinos(); assertTrue(vm.vecinos.value.cargando); advanceUntilIdle()
        assertTrue(vm.vecinos.value.desdeServidor); assertEquals("Ana", vm.vecinos.value.vecinos.single().nombre)
        repo.resultadoVecinos = ResultadoRemoto.SinConexion()
        vm.cargarVecinos(); advanceUntilIdle()
        assertFalse(vm.vecinos.value.desdeServidor); assertNotNull(vm.vecinos.value.aviso)
        assertEquals(VecinosFicticios.lista, vm.vecinos.value.vecinos)
    }
}
