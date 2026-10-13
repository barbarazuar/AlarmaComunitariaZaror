package com.example.alarmacomunitariazaror.model

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
