package com.example.alarmacomunitariazaror.util

import com.example.alarmacomunitariazaror.model.TipoEmergencia

// object = una sola instancia, sin necesidad de crear objetos. Cada función devuelve:
//   - null          -> el valor es válido
//   - un String     -> el mensaje de error que verá el usuario
object ValidadorFormulario {

    // Regex = patrón de texto. Esta acepta algo como nombre@dominio.cl
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"                  // vacío o solo espacios
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        valor.any { it.isDigit() } -> "El nombre no puede contener números"
        else -> null                                                   // todo bien
    }

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(valor.trim()) -> "Correo inválido (ej: nombre@correo.cl)"
        else -> null
    }

    fun clave(valor: String): String? = when {
        valor.isBlank() -> "La contraseña es obligatoria"
        valor.length < 6 -> "Debe tener al menos 6 caracteres"
        valor.none { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    // Compara la confirmación con la contraseña original
    fun confirmarClave(clave: String, confirmacion: String): String? = when {
        confirmacion.isBlank() -> "Repite la contraseña"
        clave != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    fun direccion(valor: String): String? = when {
        valor.isBlank() -> "La dirección es obligatoria"
        valor.trim().length < 5 -> "Escribe calle y número (ej: Los Jazmines 425)"
        else -> null
    }

    // El tipo es obligatorio: viene como null mientras el usuario no elija nada
    fun tipoEmergencia(tipo: TipoEmergencia?): String? =
        if (tipo == null) "Selecciona el tipo de emergencia" else null

    fun descripcion(valor: String): String? = when {
        valor.isNotBlank() && valor.trim().length < 5 -> "Escribe al menos 5 caracteres"
        valor.length > 280 -> "Máximo 280 caracteres (llevas ${valor.length})"
        else -> null
    }
}
