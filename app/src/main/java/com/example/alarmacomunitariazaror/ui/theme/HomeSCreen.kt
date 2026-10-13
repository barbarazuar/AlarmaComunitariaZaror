package com.example.alarmacomunitariazaror.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// estructura visual basica
fun HomeScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Alarma Comunitaria") })
        }
    ) { innerPadding ->
        // Column organiza los elementos visuales de forma vertical
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize() // ocupa todo el espacio disponible
                .padding(15.dp), // margen interno alrededor de la columna
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Bienvenidos Villa Zaror")
            Button(onClick = { /* Accion futura */ }) {
                Text("presionarme")
            }
            // Logo provisorio: cuando exista res/drawable/logo se puede cambiar por Image(painterResource(R.drawable.logo))
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Logo App",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
