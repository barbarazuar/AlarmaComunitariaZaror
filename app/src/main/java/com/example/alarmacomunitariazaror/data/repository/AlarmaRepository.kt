package com.example.alarmacomunitariazaror.data.repository

import android.content.Context
import com.example.alarmacomunitariazaror.data.local.AlertaDao
import com.example.alarmacomunitariazaror.data.local.AppDatabase
import com.example.alarmacomunitariazaror.data.local.UsuarioDao
import com.example.alarmacomunitariazaror.data.remote.AlertaRemota
import com.example.alarmacomunitariazaror.data.remote.AlertaRequestRemoto
import com.example.alarmacomunitariazaror.data.remote.AlertasApi
import com.example.alarmacomunitariazaror.data.remote.ClimaApi
import com.example.alarmacomunitariazaror.data.remote.EstadoRequestRemoto
import com.example.alarmacomunitariazaror.data.remote.LoginRequestRemoto
import com.example.alarmacomunitariazaror.data.remote.RegistroRequestRemoto
import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.data.remote.RetrofitClient
import com.example.alarmacomunitariazaror.data.remote.UsuarioRemoto
import com.example.alarmacomunitariazaror.data.remote.UsuariosApi
import com.example.alarmacomunitariazaror.data.remote.llamar
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.Clima
import com.example.alarmacomunitariazaror.model.Usuario
import com.example.alarmacomunitariazaror.model.Vecino
import com.example.alarmacomunitariazaror.model.descripcionClima
import com.example.alarmacomunitariazaror.util.sha256
import kotlinx.coroutines.flow.Flow

// Resultado de guardar una alerta: llego al servidor o quedo solo en el telefono
enum class EstadoEnvio { SINCRONIZADA, SOLO_LOCAL }

// Resultado de registrar o iniciar sesion
sealed class ResultadoAuth {
    // enLinea = false significa que se resolvio con los datos guardados en el telefono
    data class Exito(val usuario: Usuario, val enLinea: Boolean) : ResultadoAuth()
    data object CorreoDuplicado : ResultadoAuth()
    data object CredencialesInvalidas : ResultadoAuth()
    data class Error(val mensaje: String) : ResultadoAuth()
}

// Regla de datos de la app (la persona 3 la define, ver README):
//  - Room es la fuente que ve la interfaz: la pantalla siempre lee de la base local.
//  - Al crear una alerta se guarda primero en Room y despues se envia a ms-alertas.
//    Sin conexion queda como pendiente (remoteId = null) y se reenvia en la proxima sincronizacion.
//  - Actualizar o eliminar una alerta ya enviada exige conexion, para no dejar el servidor distinto.
//  - Registro e inicio de sesion usan ms-usuarios; sin conexion se acepta la cuenta guardada en el telefono.
class AlarmaRepository(
    private val alertaDao: AlertaDao,
    private val usuarioDao: UsuarioDao,
    private val alertasApi: AlertasApi,
    private val usuariosApi: UsuariosApi,
    private val climaApi: ClimaApi,
    private val urlClima: String = RetrofitClient.URL_CLIMA
) : RepositorioAlarma {
    // Historial reactivo: la UI se actualiza sola cuando cambia Room
    override val alertas: Flow<List<Alerta>> = alertaDao.observarTodas()

    // ---------------------------------------------------------------- Alertas

    // Guarda en Room y trata de enviar al microservicio. Si Room falla, lanza la excepcion.
    // El usuarioRemotoId viaja dentro de la alerta, asi tambien se envia al reintentar.
    override suspend fun guardarAlerta(alerta: Alerta): EstadoEnvio {
        val id = alertaDao.insertar(alerta.copy(sincronizada = false, remoteId = null)).toInt()
        return enviar(alerta.copy(id = id))
    }

    private suspend fun enviar(local: Alerta): EstadoEnvio =
        when (val r = llamar { alertasApi.crear(local.aRequest()) }) {
            is ResultadoRemoto.Exito -> {
                alertaDao.actualizar(local.copy(remoteId = r.datos.id, estado = r.datos.estado, sincronizada = true))
                EstadoEnvio.SINCRONIZADA
            }
            else -> EstadoEnvio.SOLO_LOCAL
        }

    // Envia las alertas creadas sin conexion. Se detiene al primer fallo de red. Devuelve cuantas subio.
    suspend fun sincronizarPendientes(): Int {
        var enviadas = 0
        for (alerta in alertaDao.pendientes()) {
            if (enviar(alerta) == EstadoEnvio.SINCRONIZADA) enviadas++ else break
        }
        return enviadas
    }

    // Sube lo pendiente y despues descarga el estado del servidor a Room.
    // Devuelve cuantas alertas hay en el servidor, o el motivo del fallo.
    override suspend fun refrescarAlertas(): ResultadoRemoto<Int> {
        sincronizarPendientes()
        return when (val r = llamar { alertasApi.listar() }) {
            is ResultadoRemoto.Exito -> {
                r.datos.forEach { guardarRemota(it) }
                alertaDao.eliminarSincronizadasFueraDe(r.datos.map { it.id })
                ResultadoRemoto.Exito(r.datos.size)
            }
            is ResultadoRemoto.ErrorHttp -> r
            is ResultadoRemoto.SinConexion -> r
        }
    }

    private suspend fun guardarRemota(remota: AlertaRemota) {
        val existente = alertaDao.buscarPorRemoteId(remota.id)
        if (existente == null) {
            alertaDao.insertar(remota.aLocal())
        } else {
            // La foto solo existe en el telefono, por eso se conserva la local
            alertaDao.actualizar(remota.aLocal().copy(id = existente.id, fotoUri = existente.fotoUri))
        }
    }

    override suspend fun cambiarEstado(alerta: Alerta, nuevoEstado: String): ResultadoRemoto<Unit> {
        val remoteId = alerta.remoteId
        if (remoteId == null) {
            alertaDao.actualizar(alerta.copy(estado = nuevoEstado))
            return ResultadoRemoto.Exito(Unit)
        }
        return when (val r = llamar { alertasApi.cambiarEstado(remoteId, EstadoRequestRemoto(nuevoEstado)) }) {
            is ResultadoRemoto.Exito -> {
                alertaDao.actualizar(alerta.copy(estado = r.datos.estado))
                ResultadoRemoto.Exito(Unit)
            }
            is ResultadoRemoto.ErrorHttp -> r
            is ResultadoRemoto.SinConexion -> r
        }
    }

    // PUT: reemplaza los datos de la alerta en el servidor y en Room
    override suspend fun editarAlerta(alerta: Alerta): ResultadoRemoto<Unit> {
        val remoteId = alerta.remoteId
        if (remoteId == null) {
            alertaDao.actualizar(alerta)
            return ResultadoRemoto.Exito(Unit)
        }
        return when (val r = llamar { alertasApi.actualizar(remoteId, alerta.aRequest()) }) {
            is ResultadoRemoto.Exito -> {
                alertaDao.actualizar(r.datos.aLocal().copy(id = alerta.id, fotoUri = alerta.fotoUri))
                ResultadoRemoto.Exito(Unit)
            }
            is ResultadoRemoto.ErrorHttp -> r
            is ResultadoRemoto.SinConexion -> r
        }
    }

    override suspend fun eliminarAlerta(alerta: Alerta): ResultadoRemoto<Unit> {
        val remoteId = alerta.remoteId
        if (remoteId != null) {
            val r = llamar { alertasApi.eliminar(remoteId) }
            // 404 = el servidor ya no la tiene, asi que igual se borra de Room
            val yaNoExiste = r is ResultadoRemoto.ErrorHttp && r.codigo == 404
            if (r !is ResultadoRemoto.Exito && !yaNoExiste) {
                return when (r) {
                    is ResultadoRemoto.ErrorHttp -> r
                    is ResultadoRemoto.SinConexion -> r
                    is ResultadoRemoto.Exito -> ResultadoRemoto.Exito(Unit)
                }
            }
        }
        alertaDao.eliminar(alerta)
        return ResultadoRemoto.Exito(Unit)
    }

    // ---------------------------------------------------------------- Usuarios

    override suspend fun registrarUsuario(nombre: String, correo: String, clave: String, direccion: String): ResultadoAuth {
        val correoNormal = correo.trim().lowercase()
        val datos = RegistroRequestRemoto(nombre.trim(), correoNormal, clave, direccion.trim())
        return when (val r = llamar { usuariosApi.registrar(datos) }) {
            is ResultadoRemoto.Exito -> ResultadoAuth.Exito(guardarCuenta(r.datos, clave), enLinea = true)
            is ResultadoRemoto.ErrorHttp ->
                if (r.codigo == 409) ResultadoAuth.CorreoDuplicado else ResultadoAuth.Error(r.mensaje)
            is ResultadoRemoto.SinConexion -> {
                // Sin servidor la cuenta se crea solo en el telefono para poder seguir usando la app
                if (usuarioDao.buscarPorCorreo(correoNormal) != null) {
                    ResultadoAuth.CorreoDuplicado
                } else {
                    val nuevo = Usuario(
                        nombre = datos.nombre, correo = correoNormal,
                        claveHash = clave.sha256(), direccion = datos.direccion
                    )
                    usuarioDao.insertar(nuevo)
                    ResultadoAuth.Exito(usuarioDao.buscarPorCorreo(correoNormal) ?: nuevo, enLinea = false)
                }
            }
        }
    }

    override suspend fun iniciarSesion(correo: String, clave: String): ResultadoAuth {
        val correoNormal = correo.trim().lowercase()
        return when (val r = llamar { usuariosApi.login(LoginRequestRemoto(correoNormal, clave)) }) {
            is ResultadoRemoto.Exito -> ResultadoAuth.Exito(guardarCuenta(r.datos, clave), enLinea = true)
            is ResultadoRemoto.ErrorHttp ->
                if (r.codigo == 401) ResultadoAuth.CredencialesInvalidas else ResultadoAuth.Error(r.mensaje)
            is ResultadoRemoto.SinConexion -> {
                val local = usuarioDao.buscarPorCorreo(correoNormal)
                when {
                    local == null -> ResultadoAuth.Error("Sin conexión y no hay una cuenta guardada en este dispositivo")
                    local.claveHash == clave.sha256() -> ResultadoAuth.Exito(local, enLinea = false)
                    else -> ResultadoAuth.CredencialesInvalidas
                }
            }
        }
    }

    // Copia la cuenta del servidor a Room para poder iniciar sesion sin conexion despues
    private suspend fun guardarCuenta(remoto: UsuarioRemoto, clave: String): Usuario {
        val existente = usuarioDao.buscarPorCorreo(remoto.correo)
        val actualizado = Usuario(
            id = existente?.id ?: 0,
            nombre = remoto.nombre,
            correo = remoto.correo,
            claveHash = clave.sha256(),
            direccion = remoto.direccion,
            remoteId = remoto.id
        )
        if (existente == null) usuarioDao.insertar(actualizado) else usuarioDao.actualizar(actualizado)
        return usuarioDao.buscarPorCorreo(remoto.correo) ?: actualizado
    }

    // Los vecinos del sector son las cuentas registradas en ms-usuarios
    override suspend fun obtenerVecinos(): ResultadoRemoto<List<Vecino>> =
        when (val r = llamar { usuariosApi.listar() }) {
            is ResultadoRemoto.Exito ->
                ResultadoRemoto.Exito(r.datos.map { Vecino(it.nombre, it.direccion, "Vecino") })
            is ResultadoRemoto.ErrorHttp -> r
            is ResultadoRemoto.SinConexion -> r
        }

    // ---------------------------------------------------------------- API externa

    override suspend fun obtenerClima(latitud: Double, longitud: Double): ResultadoRemoto<Clima> =
        when (val r = llamar { climaApi.climaActual(urlClima, latitud, longitud) }) {
            is ResultadoRemoto.Exito -> {
                val actual = r.datos.current
                if (actual == null) {
                    ResultadoRemoto.ErrorHttp(200, "La API de clima no entregó datos")
                } else {
                    ResultadoRemoto.Exito(Clima(actual.temperatura, actual.viento, descripcionClima(actual.codigo)))
                }
            }
            is ResultadoRemoto.ErrorHttp -> r
            is ResultadoRemoto.SinConexion -> r
        }

    companion object {
        fun crear(context: Context): AlarmaRepository {
            val db = AppDatabase.obtener(context)
            return AlarmaRepository(
                alertaDao = db.alertaDao(),
                usuarioDao = db.usuarioDao(),
                alertasApi = RetrofitClient.alertasApi,
                usuariosApi = RetrofitClient.usuariosApi,
                climaApi = RetrofitClient.climaApi
            )
        }
    }
}

// ---- Conversion entre el modelo de Room y el del servidor ----

private fun Alerta.aRequest() = AlertaRequestRemoto(
    vecinoNombre = vecinoNombre,
    usuarioId = usuarioRemotoId,
    ubicacion = ubicacion,
    fechaHora = fechaHora,
    tipoEmergencia = tipoEmergencia,
    descripcion = descripcion
)

private fun AlertaRemota.aLocal() = Alerta(
    vecinoNombre = vecinoNombre,
    ubicacion = ubicacion,
    fechaHora = fechaHora,
    tipoEmergencia = tipoEmergencia,
    descripcion = descripcion.orEmpty(),
    remoteId = id,
    usuarioRemotoId = usuarioId,
    estado = estado,
    sincronizada = true
)
