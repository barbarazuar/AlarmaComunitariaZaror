package com.example.alarmacomunitariazaror.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.alarmacomunitariazaror.data.repository.VecinosFicticios

// Lista de contactos del sector (datos ficticios)
@Composable
fun VecinosScreen() {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { // un elemento suelto al inicio de la lista
            Text("Contactos del sector (datos ficticios)", style = MaterialTheme.typography.titleMedium)
        }
        items(VecinosFicticios.lista) { vecino ->
            Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(vecino.nombre) },
                    supportingContent = { Text("${vecino.direccion} · ${vecino.rol}") },
                    leadingContent = { Icon(Icons.Filled.Person, contentDescription = null) }
                )
            }
        }
    }
}
