package com.example.alarmacomunitariazaror.data.repository

import com.example.alarmacomunitariazaror.data.local.AppDatabase
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.Usuario
import kotlinx.coroutines.flow.Flow

// El repositorio es el unico que habla con Room; el ViewModel no conoce la base de datos.
class AlarmaRepository(db: AppDatabase) {
    private val alertaDao = db.alertaDao()
    private val usuarioDao = db.usuarioDao()

    // Historial reactivo: la UI se actualiza sola cuando se inserta una alerta
    val alertas: Flow<List<Alerta>> = alertaDao.observarTodas()

    suspend fun guardarAlerta(alerta: Alerta) = alertaDao.insertar(alerta)

    // Devuelve true si se creo, false si ya existia ese correo
    suspend fun registrarUsuario(usuario: Usuario): Boolean {
        if (usuarioDao.buscarPorCorreo(usuario.correo) != null) return false
        usuarioDao.insertar(usuario)
        return true
    }

    suspend fun buscarUsuario(correo: String): Usuario? = usuarioDao.buscarPorCorreo(correo)
}
