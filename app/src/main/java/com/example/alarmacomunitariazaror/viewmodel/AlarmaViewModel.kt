package com.example.alarmacomunitariazaror.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmacomunitariazaror.data.remote.ResultadoRemoto
import com.example.alarmacomunitariazaror.data.repository.AlarmaRepository
import com.example.alarmacomunitariazaror.data.repository.EstadoEnvio
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.AlertaErrores
import com.example.alarmacomunitariazaror.model.AlertasUiState
import com.example.alarmacomunitariazaror.model.ClimaUiState
import com.example.alarmacomunitariazaror.model.VecinosUiState
import com.example.alarmacomunitariazaror.model.PanelUiState
import com.example.alarmacomunitariazaror.model.TipoEmergencia
import com.example.alarmacomunitariazaror.data.repository.VecinosFicticios
import com.example.alarmacomunitariazaror.util.ValidadorFormulario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlarmaViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AlarmaRepository.crear(application)

    // Historial: convierte el Flow de Room en StateFlow para que Compose lo observe.
    // WhileSubscribed(5000): deja de escuchar 5 s después de que ninguna pantalla lo usa.
    val alertas: StateFlow<List<Alerta>> = repo.alertas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private val _remoto = MutableStateFlow(AlertasUiState())
    val remoto: StateFlow<AlertasUiState> = _remoto.asStateFlow()

    private val _clima = MutableStateFlow(ClimaUiState())
    val clima: StateFlow<ClimaUiState> = _clima.asStateFlow()

    private val _vecinos = MutableStateFlow(VecinosUiState(vecinos = VecinosFicticios.lista))
    val vecinos: StateFlow<VecinosUiState> = _vecinos.asStateFlow()

    private val _panel = MutableStateFlow(PanelUiState())
    val panel: StateFlow<PanelUiState> = _panel.asStateFlow()

    fun onTipoChange(tipo: TipoEmergencia) =
        _panel.update {
            it.copy(tipo = tipo, errores = it.errores.copy(tipo = null), mensajeError = null)
        }

    fun onDescripcionChange(texto: String) =
        _panel.update {
            it.copy(descripcion = texto, errores = it.errores.copy(descripcion = null), mensajeError = null)
        }

    // Los recursos nativos (módulo 4 bis) entregan sus resultados con estas funciones
    fun onUbicacionObtenida(texto: String) = _panel.update { it.copy(ubicacion = texto) }
    fun onFotoCapturada(uri: Uri?) = _panel.update { it.copy(fotoUri = uri) }

    // El resultado se informa después de que Room termina la inserción.
    fun activarAlarma(
        vecinoNombre: String,
        usuarioRemotoId: Long? = null,
        onResultado: (Boolean) -> Unit
    ) {
        val s = _panel.value
        if (s.guardando || s.alarmaActiva) {
            onResultado(false)
            return
        }
        val errores = AlertaErrores(
            tipo = ValidadorFormulario.tipoEmergencia(s.tipo),
            descripcion = ValidadorFormulario.descripcion(s.descripcion)
        )
        val tipo = s.tipo
        if (errores.hayErrores() || tipo == null) {
            _panel.update { it.copy(errores = errores) } // muestra los errores en pantalla
            onResultado(false)
            return
        }
        val alerta = Alerta(
            vecinoNombre = vecinoNombre,
            ubicacion = s.ubicacion,
            fechaHora = System.currentTimeMillis(),   // hora actual en milisegundos
            tipoEmergencia = tipo.etiqueta,
            descripcion = s.descripcion.trim(),
            fotoUri = s.fotoUri?.toString(),
            usuarioRemotoId = usuarioRemotoId
        )
        _panel.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            runCatching { repo.guardarAlerta(alerta) }
                .onSuccess { envio ->
                    _panel.update {
                        it.copy(
                            guardando = false,
                            enviadaAlServidor = envio == EstadoEnvio.SINCRONIZADA,
                            alarmaActiva = true,
                            mensajeExito = true,
                            errores = errores
                        )
                    }
                    onResultado(true)
                    delay(3_000)
                    _panel.update { it.copy(mensajeExito = false) }
                }
                .onFailure {
                    _panel.update {
                        it.copy(
                            guardando = false,
                            mensajeError = "No se pudo guardar la alerta en este dispositivo"
                        )
                    }
                    onResultado(false)
                }
        }
    }

    // Detiene la alarma y limpia el formulario (conserva la última ubicación)
    fun desactivarAlarma() = _panel.update { PanelUiState(ubicacion = it.ubicacion) }

    // ---------- Datos remotos (ms-alertas) ----------

    // Sube lo pendiente y baja las alertas del servidor a Room. El historial se actualiza solo.
    fun refrescarAlertas() {
        if (_remoto.value.refrescando) return
        _remoto.update { it.copy(refrescando = true, mensaje = null) }
        viewModelScope.launch {
            val mensaje = when (val r = repo.refrescarAlertas()) {
                is ResultadoRemoto.Exito -> "Sincronizado: ${r.datos} alertas en el servidor"
                is ResultadoRemoto.ErrorHttp -> "El servidor respondió con error: ${r.mensaje}"
                is ResultadoRemoto.SinConexion -> "Sin conexión: mostrando los datos guardados en el teléfono"
            }
            _remoto.update { it.copy(refrescando = false, mensaje = mensaje) }
        }
    }

    fun marcarAtendida(alerta: Alerta) = operarAlerta("Alerta marcada como atendida") {
        repo.cambiarEstado(alerta, Alerta.ESTADO_ATENDIDA)
    }

    fun eliminarAlerta(alerta: Alerta) = operarAlerta("Alerta eliminada") { repo.eliminarAlerta(alerta) }

    fun editarDescripcion(alerta: Alerta, descripcion: String) = operarAlerta("Alerta actualizada") {
        repo.editarAlerta(alerta.copy(descripcion = descripcion.trim()))
    }

    private fun operarAlerta(exito: String, operacion: suspend () -> ResultadoRemoto<Unit>) {
        viewModelScope.launch {
            val mensaje = when (val r = operacion()) {
                is ResultadoRemoto.Exito -> exito
                is ResultadoRemoto.ErrorHttp -> "No se pudo completar: ${r.mensaje}"
                is ResultadoRemoto.SinConexion -> "Sin conexión: no se pudo completar la operación"
            }
            _remoto.update { it.copy(mensaje = mensaje) }
        }
    }

    fun mensajeRemotoMostrado() = _remoto.update { it.copy(mensaje = null) }

    // ---------- API externa: clima ----------
    fun cargarClima(latitud: Double = SECTOR_LATITUD, longitud: Double = SECTOR_LONGITUD) {
        if (_clima.value.cargando) return
        _clima.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            when (val r = repo.obtenerClima(latitud, longitud)) {
                is ResultadoRemoto.Exito -> _clima.update { ClimaUiState(clima = r.datos) }
                is ResultadoRemoto.ErrorHttp ->
                    _clima.update { it.copy(cargando = false, error = "No se pudo obtener el clima") }
                is ResultadoRemoto.SinConexion ->
                    _clima.update { it.copy(cargando = false, error = "Clima no disponible sin conexión") }
            }
        }
    }

    // ---------- Vecinos ----------
    fun cargarVecinos() {
        _vecinos.update { it.copy(cargando = true, aviso = null) }
        viewModelScope.launch {
            when (val r = repo.obtenerVecinos()) {
                is ResultadoRemoto.Exito -> _vecinos.update {
                    VecinosUiState(vecinos = r.datos, desdeServidor = true)
                }
                else -> _vecinos.update {
                    // Sin servidor se muestran los contactos de demostración
                    VecinosUiState(
                        vecinos = VecinosFicticios.lista,
                        aviso = "No se pudo consultar el servidor: se muestran contactos de demostración"
                    )
                }
            }
        }
    }

    companion object {
        // Sector Población Zaror, Cerrillos (aproximado)
        const val SECTOR_LATITUD = -33.498
        const val SECTOR_LONGITUD = -70.717
    }
}
