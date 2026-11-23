package com.example.alarmacomunitariazaror.viewmodel

import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.data.repository.*
import com.example.alarmacomunitariazaror.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRepositorio : RepositorioAlarma {
    override val alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val usuario = Usuario(nombre = "Ana", correo = "ana@demo.cl", claveHash = "hash", direccion = "Calle 123", remoteId = 4)
    var auth: ResultadoAuth = ResultadoAuth.Exito(usuario, true)
    var envio = EstadoEnvio.SINCRONIZADA
    var errorGuardar: Exception? = null
    var demora = 0L
    var registros = 0
    var logins = 0
    var guardadas = mutableListOf<Alerta>()
    var ultimaOperacion: Alerta? = null
    var estado: String? = null
    var resultadoOperacion: ResultadoRemoto<Unit> = ResultadoRemoto.Exito(Unit)
    var refresco: ResultadoRemoto<Int> = ResultadoRemoto.Exito(2)
    var refrescos = 0
    var resultadoClima: ResultadoRemoto<Clima> = ResultadoRemoto.Exito(Clima(20.0, 8.0, "Despejado"))
    var consultasClima = 0
    var coordenadas: Pair<Double, Double>? = null
    var resultadoVecinos: ResultadoRemoto<List<Vecino>> = ResultadoRemoto.Exito(listOf(Vecino("Ana", "Calle 123", "Vecino")))
    override suspend fun registrarUsuario(nombre: String, correo: String, clave: String, direccion: String): ResultadoAuth {
        registros++; delay(demora); return auth
    }
    override suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth {
        logins++; delay(demora); return auth
    }
    override suspend fun guardarAlerta(alerta: Alerta): EstadoEnvio {
        guardadas.add(alerta); delay(demora); errorGuardar?.let { throw it }; return envio
    }
    override suspend fun refrescarAlertas(): ResultadoRemoto<Int> {
        refrescos++; delay(demora); return refresco
    }
    override suspend fun cambiarEstado(alerta: Alerta, nuevoEstado: String): ResultadoRemoto<Unit> {
        ultimaOperacion = alerta; estado = nuevoEstado; return resultadoOperacion
    }
    override suspend fun editarAlerta(alerta: Alerta): ResultadoRemoto<Unit> {
        ultimaOperacion = alerta; return resultadoOperacion
    }
    override suspend fun eliminarAlerta(alerta: Alerta): ResultadoRemoto<Unit> {
        ultimaOperacion = alerta; return resultadoOperacion
    }
    override suspend fun obtenerClima(latitud: Double, longitud: Double): ResultadoRemoto<Clima> {
        consultasClima++; coordenadas = latitud to longitud; delay(demora); return resultadoClima
    }
    override suspend fun obtenerVecinos(): ResultadoRemoto<List<Vecino>> {
        delay(demora); return resultadoVecinos
    }
}
