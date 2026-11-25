package com.example.alarmacomunitariazaror.data.remote

import com.google.gson.annotations.SerializedName

// ---------- ms-alertas (puerto 8082) ----------
// Los nombres coinciden con los records Java AlertaRequest / AlertaResponse / EstadoRequest.

data class AlertaRequestRemoto(
    val vecinoNombre: String,
    val usuarioId: Long? = null,
    val ubicacion: String,
    val fechaHora: Long? = null,
    val tipoEmergencia: String,
    val descripcion: String? = null
)

data class AlertaRemota(
    val id: Long,
    val vecinoNombre: String,
    val usuarioId: Long? = null,
    val ubicacion: String,
    val fechaHora: Long,
    val tipoEmergencia: String,
    val descripcion: String? = null,
    val estado: String = "ACTIVA",     // "ACTIVA" o "ATENDIDA"
    val tieneFoto: Boolean = false     // true si el servidor guarda la foto de esta alerta
)

data class EstadoRequestRemoto(val estado: String)

// ---------- ms-usuarios (puerto 8081) ----------

data class RegistroRequestRemoto(
    val nombre: String,
    val correo: String,
    val clave: String,
    val direccion: String
)

data class LoginRequestRemoto(val correo: String, val clave: String)

data class ActualizarUsuarioRequestRemoto(val nombre: String, val direccion: String)

data class UsuarioRemoto(
    val id: Long,
    val nombre: String,
    val correo: String,
    val direccion: String
)

// Formato de error de los dos microservicios: { status, mensaje, errores: {campo: mensaje} }
data class ErrorRemoto(
    val status: Int = 0,
    val mensaje: String? = null,
    val errores: Map<String, String>? = null
)

// ---------- API externa: Open-Meteo (clima actual, sin clave) ----------

data class ClimaRespuesta(val current: ClimaActual? = null)

data class ClimaActual(
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("wind_speed_10m") val viento: Double,
    @SerializedName("weather_code") val codigo: Int
)
