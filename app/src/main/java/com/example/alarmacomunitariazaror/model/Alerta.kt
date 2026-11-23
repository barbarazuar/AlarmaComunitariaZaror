package com.example.alarmacomunitariazaror.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity le dice a Room: "esta clase es una tabla".
@Entity(tableName = "alertas")
data class Alerta(
    // Clave primaria; Room la numera sola (1, 2, 3...). 0 = "aun sin id".
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vecinoNombre: String,      // quien activo la alarma (dato ficticio en las pruebas)
    val ubicacion: String,         // texto aproximado, ej: "Lat -33.495, Lon -70.715"
    val fechaHora: Long,           // milisegundos desde 1970; se formatea al mostrarlo
    val tipoEmergencia: String,    // etiqueta del TipoEmergencia elegido
    val descripcion: String,       // detalle escrito por el vecino
    val fotoUri: String? = null,   // foto opcional; el "?" significa que puede ser null
    // ---- Relacion con el microservicio ms-alertas ----
    val remoteId: Long? = null,    // id en el servidor; null = aun no se ha enviado
    val usuarioRemotoId: Long? = null, // id (en ms-usuarios) de quien la activo; null = cuenta sin servidor
    val estado: String = ESTADO_ACTIVA,
    val sincronizada: Boolean = false // false = pendiente de enviar al servidor
) {
    companion object {
        const val ESTADO_ACTIVA = "ACTIVA"
        const val ESTADO_ATENDIDA = "ATENDIDA"
    }
}
