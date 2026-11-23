package com.example.alarmacomunitariazaror.data

import com.example.alarmacomunitariazaror.data.local.AlertaDao
import com.example.alarmacomunitariazaror.data.local.UsuarioDao
import com.example.alarmacomunitariazaror.data.remote.AlertaRemota
import com.example.alarmacomunitariazaror.data.remote.AlertasApi
import com.example.alarmacomunitariazaror.data.remote.ClimaApi
import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.data.remote.UsuariosApi
import com.example.alarmacomunitariazaror.data.repository.AlarmaRepository
import com.example.alarmacomunitariazaror.data.repository.EstadoEnvio
import com.example.alarmacomunitariazaror.model.Alerta
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

// Guia 15: MockK simula las dependencias del repositorio (DAO y servicios Retrofit).
// Se comprueba la regla de datos: primero Room, despues el POST al microservicio.
class AlarmaRepositoryKotestTest : StringSpec({

    val alertaDao = mockk<AlertaDao>(relaxed = true) { every { observarTodas() } returns flowOf(emptyList()) }
    val alertasApi = mockk<AlertasApi>()
    val repo = AlarmaRepository(
        alertaDao, mockk<UsuarioDao>(relaxed = true), alertasApi, mockk<UsuariosApi>(), mockk<ClimaApi>()
    )
    val alerta = Alerta(
        vecinoNombre = "Ana", ubicacion = "Calle 1", fechaHora = 1000,
        tipoEmergencia = "Incendio", descripcion = "Humo"
    )

    "si el servidor responde, la alerta queda sincronizada" {
        coEvery { alertaDao.insertar(any()) } returns 7L
        coEvery { alertasApi.crear(any()) } returns Response.success(
            AlertaRemota(
                id = 42, vecinoNombre = "Ana", ubicacion = "Calle 1", fechaHora = 1000,
                tipoEmergencia = "Incendio", descripcion = "Humo", estado = "ACTIVA"
            )
        )

        repo.guardarAlerta(alerta) shouldBe EstadoEnvio.SINCRONIZADA

        coVerify { alertaDao.actualizar(match { it.remoteId == 42L && it.sincronizada }) }
    }

    "sin conexion la alerta queda solo en el telefono y pendiente" {
        coEvery { alertaDao.insertar(any()) } returns 8L
        coEvery { alertasApi.crear(any()) } throws IOException("sin red")

        repo.guardarAlerta(alerta) shouldBe EstadoEnvio.SOLO_LOCAL

        coVerify(exactly = 0) { alertaDao.actualizar(match { it.id == 8 && it.sincronizada }) }
    }

    "eliminar una alerta no enviada solo la borra de Room" {
        val pendiente = alerta.copy(id = 3, remoteId = null)

        val r = repo.eliminarAlerta(pendiente)

        (r is ResultadoRemoto.Exito) shouldBe true
        coVerify { alertaDao.eliminar(pendiente) }
    }
})
