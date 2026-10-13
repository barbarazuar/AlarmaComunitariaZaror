package com.example.alarmacomunitariazaror.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.alarmacomunitariazaror.model.Alerta
import kotlinx.coroutines.flow.Flow

@Dao // DAO = "Data Access Object": aqui se declaran las operaciones sobre la tabla
interface AlertaDao {
    // suspend = se ejecuta en segundo plano sin congelar la pantalla
    @Insert
    suspend fun insertar(alerta: Alerta)

    // Flow = vuelve a emitir la lista cada vez que la tabla cambia.
    // Asi el historial se actualiza solo al guardarse una alerta nueva.
    @Query("SELECT * FROM alertas ORDER BY fechaHora DESC")
    fun observarTodas(): Flow<List<Alerta>>
}
