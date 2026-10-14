package com.example.alarmacomunitariazaror.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class GestorUbicacion(private val context: Context) {

    // Cliente de Google para pedir la ubicación del teléfono
    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    // Entrega el resultado como texto a través de onResultado
    @SuppressLint("MissingPermission") // revisamos el permiso manualmente antes
    fun obtener(onResultado: (String) -> Unit) {
        val fina = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val aproximada = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fina && !aproximada) {
            onResultado("Permiso de ubicación denegado")
            return
        }
        // Con permiso exacto usamos el GPS (BALANCED no lo enciende y en el emulador devuelve null)
        val prioridad = if (fina) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        cliente.getCurrentLocation(prioridad, null)
            .addOnSuccessListener { ubicacion ->
                if (ubicacion != null) {
                    onResultado(formatear(ubicacion))
                } else {
                    // Respaldo: la última ubicación conocida del teléfono
                    cliente.lastLocation
                        .addOnSuccessListener { ultima ->
                            onResultado(if (ultima != null) formatear(ultima) else "Ubicación no disponible")
                        }
                        .addOnFailureListener { onResultado("Ubicación no disponible") }
                }
            }
            .addOnFailureListener { onResultado("Ubicación no disponible") }
    }

    // 3 decimales ≈ 100 m: es una ubicación APROXIMADA a propósito (privacidad)
    private fun formatear(ubicacion: Location): String =
        "Lat %.3f, Lon %.3f".format(Locale.US, ubicacion.latitude, ubicacion.longitude)
}
