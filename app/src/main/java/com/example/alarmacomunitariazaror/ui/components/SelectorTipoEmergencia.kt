package com.example.alarmacomunitariazaror.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.alarmacomunitariazaror.model.TipoEmergencia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorTipoEmergencia(
    seleccionado: TipoEmergencia?,                 // null = aún no elige
    onSeleccion: (TipoEmergencia) -> Unit,
    error: String?
) {
    // Estado SOLO visual (si el menú está abierto): puede vivir en la pantalla
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionado?.etiqueta ?: "",   // muestra el texto de la opción elegida
            onValueChange = {},                      // no se escribe a mano
            readOnly = true,
            label = { Text("Tipo de emergencia") },
            leadingIcon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            trailingIcon = {
                if (error != null) {
                    Icon(Icons.Filled.Error, "Campo con error", tint = MaterialTheme.colorScheme.error)
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                }
            },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            // entries = todas las opciones del enum
            TipoEmergencia.entries.forEach { tipo ->
                DropdownMenuItem(
                    text = { Text(tipo.etiqueta) },
                    onClick = {
                        onSeleccion(tipo)   // avisa al ViewModel
                        expandido = false   // cierra el menú
                    }
                )
            }
        }
    }
}
