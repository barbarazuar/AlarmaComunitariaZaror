package com.example.alarmacomunitariazaror.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.model.Usuario

// Lista las tablas (entities). Si cambias una tabla, sube "version" y agrega una Migration.
@Database(entities = [Alerta::class, Usuario::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alertaDao(): AlertaDao   // Room implementa esto por ti
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        // Version 2: columnas para relacionar los datos locales con los microservicios.
        // Las alertas que ya existian quedan con remoteId = NULL, o sea "pendientes de enviar".
        val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE alertas ADD COLUMN remoteId INTEGER")
                db.execSQL("ALTER TABLE alertas ADD COLUMN usuarioRemotoId INTEGER")
                db.execSQL("ALTER TABLE alertas ADD COLUMN estado TEXT NOT NULL DEFAULT 'ACTIVA'")
                db.execSQL("ALTER TABLE alertas ADD COLUMN sincronizada INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN remoteId INTEGER")
            }
        }

        // @Volatile: todos los hilos ven siempre el valor mas reciente
        @Volatile private var instancia: AppDatabase? = null

        // Patron Singleton: una sola base de datos para toda la app
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {   // synchronized evita crear dos a la vez
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,  // contexto de la app, no de una pantalla
                    AppDatabase::class.java,
                    "alarma_comunitaria.db"      // nombre del archivo SQLite
                ).addMigrations(MIGRACION_1_2).build().also { instancia = it }
            }
    }
}
