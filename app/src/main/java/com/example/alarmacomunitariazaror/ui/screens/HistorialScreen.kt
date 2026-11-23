package com.example.alarmacomunitariazaror.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.ui.components.AlertaCard

// Recibe la lista y los callbacks para editar y eliminar alertas
@Composable
fun HistorialScreen(
    alertas: List<Alerta>,
    onActualizarAlerta: (Alerta, String) -> Unit = { _, _ -> },
    onEliminarAlerta: (Alerta) -> Unit = {}
) {
    var alertaAEditar by remember { mutableStateOf<Alerta?>(null) }
    var textoEdicion by remember { mutableStateOf("") }
    var alertaAEliminar by remember { mutableStateOf<Alerta?>(null) }
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }

    // Diálogo para editar descripción de la alerta
    alertaAEditar?.let { alerta ->
        AlertDialog(
            onDismissRequest = { alertaAEditar = null },
            title = { Text("Editar descripción de alerta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tipo: ${alerta.tipoEmergencia}", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = textoEdicion,
                        onValueChange = { textoEdicion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (textoEdicion.isNotBlank()) {
                            onActualizarAlerta(alerta, textoEdicion)
                            alertaAEditar = null
                            mensajeFeedback = "Alerta modificada con éxito"
                        }
                    }
                ) { Text("Guardar cambios") }
            },
            dismissButton = {
                TextButton(onClick = { alertaAEditar = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de confirmación para eliminar alerta
    alertaAEliminar?.let { alerta ->
        AlertDialog(
            onDismissRequest = { alertaAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Deseas eliminar esta alerta del historial? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarAlerta(alerta)
                        alertaAEliminar = null
                        mensajeFeedback = "Alerta eliminada del historial"
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { alertaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        // Banner animado de confirmación cuando una operación se completa con éxito
        AnimatedVisibility(
            visible = mensajeFeedback != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mensajeFeedback.orEmpty(),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { mensajeFeedback = null }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cerrar", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        if (alertas.isEmpty()) {
            // Estado vacío: mensaje amable en vez de una pantalla en blanco
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(64.dp))
                    Text("Aún no hay alertas registradas")
                }
            }
        } else {
            // LazyColumn = lista que solo dibuja lo visible (eficiente con muchas alertas)
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alertas, key = { it.id }) { alerta ->
                    AlertaCard(
                        alerta = alerta,
                        onEditar = {
                            alertaAEditar = alerta
                            textoEdicion = alerta.descripcion
                        },
                        onEliminar = {
                            alertaAEliminar = alerta
                        }
                    )
                }
            }
        }
    }
}
