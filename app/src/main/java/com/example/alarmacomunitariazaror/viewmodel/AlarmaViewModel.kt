package com.example.alarmacomunitariazaror.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmacomunitariazaror.data.local.AppDatabase
import com.example.alarmacomunitariazaror.data.repository.AlarmaRepository
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.AlertaErrores
import com.example.alarmacomunitariazaror.model.PanelUiState
import com.example.alarmacomunitariazaror.model.TipoEmergencia
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

    private val repo = AlarmaRepository(AppDatabase.obtener(application))

    // Historial: convierte el Flow de Room en StateFlow para que Compose lo observe.
    // WhileSubscribed(5000): deja de escuchar 5 s después de que ninguna pantalla lo usa.
    val alertas: StateFlow<List<Alerta>> = repo.alertas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

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
    fun activarAlarma(vecinoNombre: String, onResultado: (Boolean) -> Unit) {
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
            fotoUri = s.fotoUri?.toString()
        )
        _panel.update { it.copy(guardando = true, mensajeError = null) }
        viewModelScope.launch {
            runCatching { repo.guardarAlerta(alerta) }
                .onSuccess {
                    _panel.update {
                        it.copy(
                            guardando = false,
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
}
