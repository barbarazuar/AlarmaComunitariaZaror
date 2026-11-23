package com.example.alarmacomunitariazaror.model

// Clima actual del sector, ya traducido para mostrarlo (viene de la API externa Open-Meteo)
data class Clima(
    val temperatura: Double,   // grados Celsius
    val viento: Double,        // km/h
    val descripcion: String    // ej: "Despejado", "Lluvia"
)

// Traduce el codigo WMO que entrega Open-Meteo a un texto en espanol
fun descripcionClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1, 2 -> "Mayormente despejado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    in 51..57 -> "Llovizna"
    in 61..67 -> "Lluvia"
    in 71..77 -> "Nieve"
    in 80..82 -> "Chubascos"
    85, 86 -> "Nevazón"
    in 95..99 -> "Tormenta eléctrica"
    else -> "Sin datos"
}
