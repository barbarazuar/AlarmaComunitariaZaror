package com.example.alarmacomunitariazaror.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// indices unique = no pueden existir dos usuarios con el mismo correo.
@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val correo: String,
    val claveHash: String,   // NUNCA guardamos la contrasena tal cual, solo su "huella" SHA-256
    val direccion: String    // ej: "Los Jazmines 425" (ficticia en pruebas)
)
