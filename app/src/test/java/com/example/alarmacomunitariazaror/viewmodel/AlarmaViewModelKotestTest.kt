package com.example.alarmacomunitariazaror.viewmodel

import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.data.repository.EstadoEnvio
import com.example.alarmacomunitariazaror.data.repository.RepositorioAlarma
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.TipoEmergencia
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// Guia 15: Kotest para escribir los casos y MockK para simular el repositorio.
// Se prueba la logica del ViewModel sin Room, sin red y sin Android.
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmaViewModelKotestTest : StringSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    fun repoSimulado(): RepositorioAlarma = mockk(relaxed = true) {
        every { alertas } returns flowOf(emptyList())
    }

    val alerta = Alerta(
        vecinoNombre = "Ana", ubicacion = "Calle 1", fechaHora = 1000,
        tipoEmergencia = "Incendio", descripcion = "Humo"
    )

    "activar la alarma con datos validos guarda la alerta en el repositorio" {
        val repo = repoSimulado()
        coEvery { repo.guardarAlerta(any()) } returns EstadoEnvio.SINCRONIZADA
        val vm = AlarmaViewModel(repo)
        vm.onTipoChange(TipoEmergencia.entries.first())
        vm.onDescripcionChange("Humo en el pasaje")

        var resultado: Boolean? = null
        vm.activarAlarma("Ana") { resultado = it }

        resultado shouldBe true
        vm.panel.value.alarmaActiva shouldBe true
        vm.panel.value.enviadaAlServidor shouldBe true
        coVerify(exactly = 1) { repo.guardarAlerta(any()) }
    }

    "sin tipo de emergencia no se llama al repositorio" {
        val repo = repoSimulado()
        val vm = AlarmaViewModel(repo)

        var resultado: Boolean? = null
        vm.activarAlarma("Ana") { resultado = it }

        resultado shouldBe false
        coVerify(exactly = 0) { repo.guardarAlerta(any()) }
    }

    "marcar atendida pide al repositorio cambiar el estado y avisa el resultado" {
        val repo = repoSimulado()
        coEvery { repo.cambiarEstado(any(), any()) } returns ResultadoRemoto.Exito(Unit)
        val vm = AlarmaViewModel(repo)

        vm.marcarAtendida(alerta)

        coVerify { repo.cambiarEstado(alerta, Alerta.ESTADO_ATENDIDA) }
        vm.remoto.value.mensaje shouldBe "Alerta marcada como atendida"
    }

    "eliminar sin conexion muestra el aviso y no lanza excepcion" {
        val repo = repoSimulado()
        coEvery { repo.eliminarAlerta(any()) } returns ResultadoRemoto.SinConexion()
        val vm = AlarmaViewModel(repo)

        vm.eliminarAlerta(alerta)

        vm.remoto.value.mensaje!! shouldContain "Sin conexión"
    }

    "editar la descripcion recorta los espacios antes de enviar" {
        val repo = repoSimulado()
        coEvery { repo.editarAlerta(any()) } returns ResultadoRemoto.Exito(Unit)
        val vm = AlarmaViewModel(repo)

        vm.editarDescripcion(alerta, "  Nueva descripcion  ")

        coVerify { repo.editarAlerta(match { it.descripcion == "Nueva descripcion" }) }
    }
})
