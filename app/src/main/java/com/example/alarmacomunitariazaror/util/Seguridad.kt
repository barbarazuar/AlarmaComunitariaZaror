package com.example.alarmacomunitariazaror.util

import java.security.MessageDigest

// Funcion de extension: se usa como "miClave".sha256()
fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
    // Convierte cada byte a 2 caracteres hexadecimales y los une en un solo texto
    return bytes.joinToString("") { "%02x".format(it) }
}
