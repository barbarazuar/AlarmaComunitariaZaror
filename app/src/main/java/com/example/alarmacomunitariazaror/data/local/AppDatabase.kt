package com.example.alarmacomunitariazaror.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.Usuario

// Lista las tablas (entities). Si cambias una tabla, sube "version" (o desinstala la app en pruebas).
@Database(entities = [Alerta::class, Usuario::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertaDao(): AlertaDao   // Room implementa esto por ti
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        // @Volatile: todos los hilos ven siempre el valor mas reciente
        @Volatile private var instancia: AppDatabase? = null

        // Patron Singleton: una sola base de datos para toda la app
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {   // synchronized evita crear dos a la vez
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,  // contexto de la app, no de una pantalla
                    AppDatabase::class.java,
                    "alarma_comunitaria.db"      // nombre del archivo SQLite
                ).build().also { instancia = it }
            }
    }
}
