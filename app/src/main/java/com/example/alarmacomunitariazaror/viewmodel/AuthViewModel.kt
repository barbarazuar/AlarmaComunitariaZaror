package com.example.alarmacomunitariazaror.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.example.alarmacomunitariazaror.data.repository.AlarmaRepository
import com.example.alarmacomunitariazaror.data.repository.RepositorioAlarma
import com.example.alarmacomunitariazaror.data.repository.ResultadoAuth
import com.example.alarmacomunitariazaror.model.LoginUiState
import com.example.alarmacomunitariazaror.model.RegistroErrores
import com.example.alarmacomunitariazaror.model.RegistroUiState
import com.example.alarmacomunitariazaror.model.Usuario
import com.example.alarmacomunitariazaror.util.ValidadorFormulario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El repositorio se recibe por constructor; la fábrica lo conecta con Android.
class AuthViewModel(private val repo: RepositorioAlarma) : ViewModel() {

    // Patrón _privado / público: solo el ViewModel modifica; la UI solo lee.
    private val _registro = MutableStateFlow(RegistroUiState())
    val registro: StateFlow<RegistroUiState> = _registro.asStateFlow()

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    // Usuario con sesión iniciada (null = nadie ha entrado)
    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    // ---------- Cambios de campos del REGISTRO ----------
    // Cada función guarda el valor nuevo y limpia el error de ese campo mientras se escribe.
    fun onRegNombre(v: String) = _registro.update { it.copy(nombre = v, errores = it.errores.copy(nombre = null)) }
    fun onRegCorreo(v: String) = _registro.update { it.copy(correo = v, errores = it.errores.copy(correo = null)) }
    fun onRegClave(v: String) = _registro.update { it.copy(clave = v, errores = it.errores.copy(clave = null)) }
    fun onRegConfirmar(v: String) = _registro.update { it.copy(confirmarClave = v, errores = it.errores.copy(confirmar = null)) }
    fun onRegDireccion(v: String) = _registro.update { it.copy(direccion = v, errores = it.errores.copy(direccion = null)) }
    fun onRegTerminos(v: Boolean) = _registro.update { it.copy(aceptaTerminos = v, errores = it.errores.copy(terminos = null)) }

    fun registrar() {
        val s = _registro.value
        // 1) Validar TODOS los campos con el validador central
        val errores = RegistroErrores(
            nombre = ValidadorFormulario.nombre(s.nombre),
            correo = ValidadorFormulario.correo(s.correo),
            clave = ValidadorFormulario.clave(s.clave),
            confirmar = ValidadorFormulario.confirmarClave(s.clave, s.confirmarClave),
            direccion = ValidadorFormulario.direccion(s.direccion),
            terminos = if (!s.aceptaTerminos) "Debes aceptar los términos" else null
        )
        _registro.update { it.copy(errores = errores) }
        if (errores.hayErrores()) return // si hay errores, se detiene aquí

        // 2) Registrar en ms-usuarios (o solo en Room si no hay conexión) dentro de una corrutina
        viewModelScope.launch {
            _registro.update { it.copy(cargando = true, errorGeneral = null) }
            val resultado = repo.registrarUsuario(s.nombre, s.correo, s.clave, s.direccion)
            _registro.update {
                when (resultado) {
                    is ResultadoAuth.Exito -> it.copy(cargando = false, registroExitoso = true)
                    ResultadoAuth.CorreoDuplicado -> it.copy(
                        cargando = false,
                        errores = it.errores.copy(correo = "Este correo ya está registrado")
                    )
                    is ResultadoAuth.Error -> it.copy(cargando = false, errorGeneral = resultado.mensaje)
                    ResultadoAuth.CredencialesInvalidas -> it.copy(cargando = false)
                }
            }
        }
    }

    // La pantalla llama a esto después de navegar, para limpiar el formulario
    fun registroConsumido() { _registro.value = RegistroUiState() }

    // ---------- LOGIN ----------
    fun onLoginCorreo(v: String) = _login.update { it.copy(correo = v, errorCorreo = null, errorGeneral = null) }
    fun onLoginClave(v: String) = _login.update { it.copy(clave = v, errorClave = null, errorGeneral = null) }

    fun iniciarSesion() {
        val s = _login.value
        val errCorreo = ValidadorFormulario.correo(s.correo)
        val errClave = if (s.clave.isBlank()) "Ingresa tu contraseña" else null
        if (errCorreo != null || errClave != null) {
            _login.update { it.copy(errorCorreo = errCorreo, errorClave = errClave) }
            return
        }
        viewModelScope.launch {
            _login.update { it.copy(cargando = true, errorGeneral = null) }
            when (val r = repo.iniciarSesion(s.correo, s.clave)) {
                is ResultadoAuth.Exito -> {
                    _usuario.value = r.usuario                           // guarda la sesión
                    _login.update { it.copy(cargando = false, sesionIniciada = true) }
                }
                is ResultadoAuth.Error ->
                    _login.update { it.copy(cargando = false, errorGeneral = r.mensaje) }
                else ->
                    _login.update { it.copy(cargando = false, errorGeneral = "Correo o contraseña incorrectos") }
            }
        }
    }

    fun loginConsumido() { _login.value = LoginUiState() }

    fun cerrarSesion() {
        _usuario.value = null
        _login.value = LoginUiState()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AuthViewModel(AlarmaRepository.crear(checkNotNull(this[APPLICATION_KEY]))) }
        }
    }
}
