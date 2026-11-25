package com.example.alarmacomunitariazaror.nativo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

object ManejadorFoto {
    // Crea un archivo vacio en la carpeta privada de la app y devuelve su direccion (Uri)
    // para que la camara guarde ahi la foto.
    fun crearUriTemporal(context: Context): Uri {
        val carpeta = File(context.filesDir, "fotos").apply { mkdirs() } // crea la carpeta si no existe
        val archivo = File.createTempFile("alerta_", ".jpg", carpeta)
        // "${packageName}.fileprovider" debe coincidir con android:authorities del Manifest
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    // Lee la foto de una alerta y la reduce (lado mayor de 1280 px, JPEG al 80 %) para que
    // pese poco al subirla al servidor. Devuelve null si el archivo ya no existe o no es una imagen.
    // Debe llamarse fuera del hilo principal.
    fun leerYReducir(context: Context, uri: String, maxLado: Int = 1280): ByteArray? = try {
        val resolver = context.contentResolver
        val direccion = Uri.parse(uri)
        // Primero solo se miden las dimensiones, sin cargar la imagen completa en memoria
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(direccion)?.use { BitmapFactory.decodeStream(it, null, medidas) }
        var muestra = 1
        while (medidas.outWidth / muestra > maxLado * 2 || medidas.outHeight / muestra > maxLado * 2) muestra *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        val bitmap = resolver.openInputStream(direccion)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        if (bitmap == null) {
            null
        } else {
            val escala = maxLado.toFloat() / maxOf(bitmap.width, bitmap.height)
            val final = if (escala < 1f) {
                Bitmap.createScaledBitmap(bitmap, (bitmap.width * escala).toInt(), (bitmap.height * escala).toInt(), true)
            } else bitmap
            ByteArrayOutputStream().use { salida ->
                final.compress(Bitmap.CompressFormat.JPEG, 80, salida)
                salida.toByteArray()
            }
        }
    } catch (e: Exception) {
        null
    }

    // Guarda en la carpeta de fotos una imagen descargada del servidor y devuelve su direccion (Uri)
    fun guardarDescargada(context: Context, remoteId: Long, bytes: ByteArray): String? = try {
        val carpeta = File(context.filesDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "remota_$remoteId.jpg").apply { writeBytes(bytes) }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo).toString()
    } catch (e: Exception) {
        null
    }
}
