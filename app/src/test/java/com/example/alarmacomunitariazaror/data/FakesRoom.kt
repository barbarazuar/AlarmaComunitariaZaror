package com.example.alarmacomunitariazaror.data

import com.example.alarmacomunitariazaror.data.local.AlertaDao
import com.example.alarmacomunitariazaror.data.local.UsuarioDao
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// DAOs en memoria para probar el repositorio sin Android ni base de datos real

class FakeAlertaDao : AlertaDao {
    val datos = mutableListOf<Alerta>()
    private var siguienteId = 1
    private val flujo = MutableStateFlow<List<Alerta>>(emptyList())

    private fun emitir() { flujo.value = datos.sortedByDescending { it.fechaHora } }

    override suspend fun insertar(alerta: Alerta): Long {
        val id = if (alerta.id == 0) siguienteId++ else alerta.id
        datos.add(alerta.copy(id = id))
        emitir()
        return id.toLong()
    }

    override suspend fun actualizar(alerta: Alerta) {
        val i = datos.indexOfFirst { it.id == alerta.id }
        if (i >= 0) datos[i] = alerta
        emitir()
    }

    override suspend fun eliminar(alerta: Alerta) {
        datos.removeAll { it.id == alerta.id }
        emitir()
    }

    override fun observarTodas(): Flow<List<Alerta>> = flujo

    override suspend fun pendientes(): List<Alerta> = datos.filter { it.remoteId == null }

    override suspend fun buscarPorRemoteId(remoteId: Long): Alerta? = datos.firstOrNull { it.remoteId == remoteId }

    override suspend fun eliminarSincronizadasFueraDe(idsVigentes: List<Long>) {
        datos.removeAll { it.remoteId != null && it.remoteId !in idsVigentes }
        emitir()
    }
}

class FakeUsuarioDao : UsuarioDao {
    val datos = mutableListOf<Usuario>()
    private var siguienteId = 1

    override suspend fun insertar(usuario: Usuario) {
        datos.add(usuario.copy(id = siguienteId++))
    }

    override suspend fun actualizar(usuario: Usuario) {
        val i = datos.indexOfFirst { it.id == usuario.id }
        if (i >= 0) datos[i] = usuario
    }

    override suspend fun buscarPorCorreo(correo: String): Usuario? = datos.firstOrNull { it.correo == correo }
}
