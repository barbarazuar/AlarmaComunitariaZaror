package com.example.alarmacomunitariazaror.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

// Cada interfaz describe un microservicio; Retrofit genera la implementacion.
// Devuelven Response<T> para poder leer el codigo HTTP y el cuerpo del error.

interface AlertasApi {
    @GET("api/alertas")
    suspend fun listar(
        @Query("estado") estado: String? = null,
        @Query("usuarioId") usuarioId: Long? = null
    ): Response<List<AlertaRemota>>

    @GET("api/alertas/{id}")
    suspend fun obtener(@Path("id") id: Long): Response<AlertaRemota>

    @POST("api/alertas")
    suspend fun crear(@Body alerta: AlertaRequestRemoto): Response<AlertaRemota>

    @PUT("api/alertas/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body alerta: AlertaRequestRemoto): Response<AlertaRemota>

    @PATCH("api/alertas/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body estado: EstadoRequestRemoto): Response<AlertaRemota>

    @DELETE("api/alertas/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}

interface UsuariosApi {
    @POST("api/usuarios/registro")
    suspend fun registrar(@Body datos: RegistroRequestRemoto): Response<UsuarioRemoto>

    @POST("api/usuarios/login")
    suspend fun login(@Body datos: LoginRequestRemoto): Response<UsuarioRemoto>

    @GET("api/usuarios")
    suspend fun listar(): Response<List<UsuarioRemoto>>

    @GET("api/usuarios/{id}")
    suspend fun obtener(@Path("id") id: Long): Response<UsuarioRemoto>

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body datos: ActualizarUsuarioRequestRemoto): Response<UsuarioRemoto>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}

// API externa de clima (Open-Meteo). La URL completa va con @Url porque es otro dominio.
interface ClimaApi {
    @GET
    suspend fun climaActual(
        @Url url: String,
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") campos: String = "temperature_2m,wind_speed_10m,weather_code"
    ): Response<ClimaRespuesta>
}
