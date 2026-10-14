package com.example.alarmacomunitariazaror.nativo

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager

// object = una sola instancia en toda la app. Así la alarma puede detenerse
// desde cualquier pantalla y nunca quedan dos sonidos a la vez.
object GestorSonido {
    private var reproductor: MediaPlayer? = null

    fun iniciar(context: Context) {
        detener() // por si ya había uno sonando
        try {
            // Usamos el tono de alarma del propio teléfono: no hay que agregar archivos de audio
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            reproductor = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)            // suena por el volumen de alarma
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context.applicationContext, uri)            // qué sonido reproducir
                isLooping = true                                          // se repite hasta detenerlo
                prepare()                                                 // carga el sonido
                start()                                                   // ¡suena!
            }
        } catch (e: Exception) {
            reproductor = null // si algo falla (ej: emulador sin tono), la app sigue funcionando
        }
    }

    fun detener() {
        reproductor?.let {
            if (it.isPlaying) it.stop()
            it.release()          // libera memoria: IMPORTANTE siempre
        }
        reproductor = null
    }
}
