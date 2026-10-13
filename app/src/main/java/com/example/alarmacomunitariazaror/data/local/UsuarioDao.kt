package com.example.alarmacomunitariazaror.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.alarmacomunitariazaror.model.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario)

    // :correo se reemplaza por el parametro de la funcion. Devuelve null si no existe.
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?
}
