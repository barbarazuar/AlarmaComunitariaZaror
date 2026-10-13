package com.example.alarmacomunitariazaror.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.util.comoFechaLegible

// Una tarjeta = una alerta del historial
@Composable
fun AlertaCard(alerta: Alerta) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text(alerta.tipoEmergencia, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(alerta.descripcion)
            Text("Activada por ${alerta.vecinoNombre}", style = MaterialTheme.typography.bodySmall)
            Text(
                "${alerta.fechaHora.comoFechaLegible()} · ${alerta.ubicacion}",
                style = MaterialTheme.typography.bodySmall
            )
            // let { } se ejecuta solo si fotoUri no es null
            alerta.fotoUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Evidencia fotográfica",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
