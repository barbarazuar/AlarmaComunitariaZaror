package com.example.alarmacomunitariazaror.data.repository

import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.Clima
import com.example.alarmacomunitariazaror.model.Vecino
import kotlinx.coroutines.flow.Flow

// Contrato de datos que permite probar los ViewModel sin Android, Room ni red.
interface RepositorioAlarma {
    val alertas: Flow<List<Alerta>>
    suspend fun guardarAlerta(alerta: Alerta): EstadoEnvio
    suspend fun refrescarAlertas(): ResultadoRemoto<Int>
    suspend fun cambiarEstado(alerta: Alerta, nuevoEstado: String): ResultadoRemoto<Unit>
    suspend fun editarAlerta(alerta: Alerta): ResultadoRemoto<Unit>
    suspend fun eliminarAlerta(alerta: Alerta): ResultadoRemoto<Unit>
    suspend fun registrarUsuario(nombre: String, correo: String, clave: String, direccion: String): ResultadoAuth
    suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth
    suspend fun obtenerVecinos(): ResultadoRemoto<List<Vecino>>
    suspend fun obtenerClima(latitud: Double, longitud: Double): ResultadoRemoto<Clima>
}
