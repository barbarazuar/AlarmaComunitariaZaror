package com.example.alarmacomunitariazaror.model

import android.net.Uri

// ---------- REGISTRO ----------
// Un mensaje de error (o null) por cada campo del formulario
data class RegistroErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val confirmar: String? = null,
    val direccion: String? = null,
    val terminos: String? = null
) {
    // true si al menos un campo tiene error
    fun hayErrores(): Boolean =
        listOfNotNull(nombre, correo, clave, confirmar, direccion, terminos).isNotEmpty()
}

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    val direccion: String = "",
    val aceptaTerminos: Boolean = false,
    val errores: RegistroErrores = RegistroErrores(),
    val cargando: Boolean = false,        // muestra la ruedita en el botón
    val registroExitoso: Boolean = false  // avisa a la pantalla que debe navegar
)

// ---------- LOGIN ----------
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null,     // "Correo o contraseña incorrectos"
    val cargando: Boolean = false,
    val sesionIniciada: Boolean = false
)

// ---------- PANEL DE ALARMA (formulario de nueva alerta) ----------
data class AlertaErrores(val tipo: String? = null, val descripcion: String? = null) {
    fun hayErrores(): Boolean = tipo != null || descripcion != null
}

data class PanelUiState(
    val tipo: TipoEmergencia? = null,
    val descripcion: String = "",
    val errores: AlertaErrores = AlertaErrores(),
    val ubicacion: String = "Ubicación no disponible",
    val fotoUri: Uri? = null,             // foto opcional tomada con la cámara
    val guardando: Boolean = false,
    val alarmaActiva: Boolean = false,    // true mientras suena la alarma
    val mensajeExito: Boolean = false,
    val mensajeError: String? = null
)
