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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.ui.components.AlertaCard

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
    onEliminar: ((Alerta) -> Unit)? = null
) {
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
                        onEliminar = onEliminar?.let { { it(alerta) } }
                    )
                }
            }
        }
    }
}
