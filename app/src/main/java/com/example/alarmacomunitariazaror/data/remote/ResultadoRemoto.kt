package com.example.alarmacomunitariazaror.data.remote

import com.google.gson.Gson
import java.io.IOException
import retrofit2.Response

// Resultado de una llamada de red. Separa los tres casos que la app trata distinto:
// funciono, el servidor respondio con error, o no hubo conexion.
sealed class ResultadoRemoto<out T> {
    data class Exito<T>(val datos: T) : ResultadoRemoto<T>()
    data class ErrorHttp(val codigo: Int, val mensaje: String) : ResultadoRemoto<Nothing>()
    data class SinConexion(val causa: String = "Sin conexión con el servidor") : ResultadoRemoto<Nothing>()
}

private val gson = Gson()

// Ejecuta una llamada Retrofit y la convierte en ResultadoRemoto sin lanzar excepciones de red.
suspend fun <T> llamar(bloque: suspend () -> Response<T>): ResultadoRemoto<T> =
    try {
        val r = bloque()
        if (r.isSuccessful) {
            @Suppress("UNCHECKED_CAST")
            val cuerpo = (r.body() ?: Unit) as T      // 204 (DELETE) no trae cuerpo
            ResultadoRemoto.Exito(cuerpo)
        } else {
            ResultadoRemoto.ErrorHttp(r.code(), mensajeDeError(r))
        }
    } catch (e: IOException) {
        // Incluye UnknownHost, ConnectException y SocketTimeoutException
        ResultadoRemoto.SinConexion(e.message ?: "Sin conexión con el servidor")
    }

// Lee { mensaje, errores } del cuerpo de error; si no se puede, usa un texto generico.
internal fun mensajeDeError(r: Response<*>): String {
    val texto = runCatching { r.errorBody()?.string() }.getOrNull()
    val error = runCatching { gson.fromJson(texto, ErrorRemoto::class.java) }.getOrNull()
    val detalle = error?.errores?.values?.firstOrNull()
    return detalle ?: error?.mensaje ?: "Error ${r.code()} del servidor"
}
