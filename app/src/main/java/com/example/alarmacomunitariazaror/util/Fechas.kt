package com.example.alarmacomunitariazaror.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Convierte los milisegundos guardados en Room a "10/10/2026 01:30"
fun Long.comoFechaLegible(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(this))
