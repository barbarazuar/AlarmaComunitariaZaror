package com.example.alarmacomunitariazaror.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.ui.components.AlertaCard
import com.example.alarmacomunitariazaror.util.ValidadorFormulario

// Recibe la lista ya lista para mostrar: no conoce Room ni el ViewModel.
// Deslizar hacia abajo sincroniza con el servidor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    alertas: List<Alerta>,
    refrescando: Boolean = false,
    mensaje: String? = null,
    onRefrescar: () -> Unit = {},
    onAtender: ((Alerta) -> Unit)? = null,
    onEditar: ((Alerta, String) -> Unit)? = null,
    onEliminar: ((Alerta) -> Unit)? = null
) {
    var alertaAEditar by remember { mutableStateOf<Alerta?>(null) }
    var textoEdicion by remember { mutableStateOf("") }
    var alertaAEliminar by remember { mutableStateOf<Alerta?>(null) }
    val errorEdicion = ValidadorFormulario.descripcionEdicion(textoEdicion)

    // Dialogo para editar la descripcion
    alertaAEditar?.let { alerta ->
        AlertDialog(
            onDismissRequest = { alertaAEditar = null },
            title = { Text("Editar descripción de alerta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tipo: ${alerta.tipoEmergencia}")
                    OutlinedTextField(
                        value = textoEdicion,
                        onValueChange = { textoEdicion = it },
                        label = { Text("Descripción") },
                        isError = errorEdicion != null,
                        supportingText = { if (errorEdicion != null) Text(errorEdicion) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = errorEdicion == null,
                    onClick = {
                        onEditar?.invoke(alerta, textoEdicion)
                        alertaAEditar = null
                    }
                ) { Text("Guardar cambios") }
            },
            dismissButton = { TextButton(onClick = { alertaAEditar = null }) { Text("Cancelar") } }
        )
    }

    // Confirmacion antes de eliminar
    alertaAEliminar?.let { alerta ->
        AlertDialog(
            onDismissRequest = { alertaAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Deseas eliminar esta alerta del historial? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar?.invoke(alerta)
                    alertaAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { alertaAEliminar = null }) { Text("Cancelar") } }
        )
    }

    PullToRefreshBox(isRefreshing = refrescando, onRefresh = onRefrescar, modifier = Modifier.fillMaxSize()) {
        if (alertas.isEmpty()) {
            // Estado vacio: mensaje amable en vez de una pantalla en blanco
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(64.dp))
                    Text("Aún no hay alertas registradas")
                    if (mensaje != null) Text(mensaje, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            // LazyColumn = lista que solo dibuja lo visible (eficiente con muchas alertas)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (mensaje != null) {
                    item { Text(mensaje, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth()) }
                }
                items(alertas, key = { it.id }) { alerta ->
                    AlertaCard(
                        alerta = alerta,
                        onAtender = onAtender?.let { { it(alerta) } },
                        onEditar = onEditar?.let {
                            { alertaAEditar = alerta; textoEdicion = alerta.descripcion }
                        },
                        onEliminar = onEliminar?.let { { alertaAEliminar = alerta } }
                    )
                }
            }
        }
    }
}
