package com.example.alarmacomunitariazaror.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.alarmacomunitariazaror.MainActivity

class Notificador(private val context: Context) {

    companion object {
        const val CANAL_ID = "alarma_comunitaria_urgente"
    }

    // Se ejecuta al crear el objeto: registra el canal de notificaciones
    init {
        // Desde Android 8 (API 26) toda notificación necesita un "canal"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ID,
                "Alertas comunitarias",                   // nombre que ve el usuario en Ajustes
                NotificationManager.IMPORTANCE_HIGH       // aparece como aviso emergente
            ).apply { description = "Avisos urgentes de emergencias del sector" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    @SuppressLint("MissingPermission") // el permiso se revisa manualmente justo abajo
    fun mostrarAlerta(titulo: String, detalle: String) {
        // Android 13+ exige permiso. Si no lo dieron, simplemente no mostramos nada.
        val permitido = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!permitido) return

        // Al tocar la notificación se abre la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)      // ícono del sistema (sin crear imágenes)
            .setContentTitle(titulo)
            .setContentText(detalle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendiente)
            .setAutoCancel(true)                                   // se borra al tocarla
            .build()

        // El id distinto por notificación permite que lleguen varias
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacion)
    }
}
