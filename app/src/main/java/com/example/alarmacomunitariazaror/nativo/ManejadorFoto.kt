package com.example.alarmacomunitariazaror.nativo

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
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
}
