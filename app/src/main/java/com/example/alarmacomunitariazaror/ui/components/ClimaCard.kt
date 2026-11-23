package com.example.alarmacomunitariazaror.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmacomunitariazaror.model.ClimaUiState

// Muestra el clima actual del sector (dato de la API externa Open-Meteo)
@Composable
fun ClimaCard(estado: ClimaUiState) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Cloud, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            when {
                estado.cargando -> {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Consultando el clima...")
                }
                estado.clima != null -> Column {
                    Text(
                        "${estado.clima.temperatura} °C · ${estado.clima.descripcion}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "Viento ${estado.clima.viento} km/h · Open-Meteo",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                else -> Text(estado.error ?: "Clima no disponible", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
