package com.example.alarmacomunitariazaror.data.remote

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Crea los clientes Retrofit una sola vez.
// En el emulador, 10.0.2.2 es el PC que corre Spring Boot. En un telefono fisico
// hay que cambiar HOST por la IP del PC en la red WiFi (ver README).
object RetrofitClient {
    const val HOST = "10.0.2.2"
    const val URL_USUARIOS = "http://$HOST:8081/"
    const val URL_ALERTAS = "http://$HOST:8082/"
    const val URL_CLIMA = "https://api.open-meteo.com/v1/forecast"

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)   // sin servidor falla rapido y la app sigue con Room
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
    }

    fun <T> crear(baseUrl: String, servicio: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(servicio)

    val alertasApi: AlertasApi by lazy { crear(URL_ALERTAS, AlertasApi::class.java) }
    val usuariosApi: UsuariosApi by lazy { crear(URL_USUARIOS, UsuariosApi::class.java) }
    // baseUrl de relleno: ClimaApi usa @Url con la direccion completa
    val climaApi: ClimaApi by lazy { crear("https://api.open-meteo.com/", ClimaApi::class.java) }
}
