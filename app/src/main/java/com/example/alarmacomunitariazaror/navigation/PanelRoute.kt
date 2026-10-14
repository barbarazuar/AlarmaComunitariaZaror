package com.example.alarmacomunitariazaror.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.alarmacomunitariazaror.ui.screens.PanelScreen
import com.example.alarmacomunitariazaror.viewmodel.AlarmaViewModel
import com.example.alarmacomunitariazaror.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelRoute(
    authViewModel: AuthViewModel,
    alarmaViewModel: AlarmaViewModel,
    onIrAHistorial: () -> Unit,
    onIrAVecinos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val panel by alarmaViewModel.panel.collectAsState()
    val usuario by authViewModel.usuario.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DEMO ACADÉMICA") },
                actions = {
                    IconButton(onClick = onIrAHistorial) {
                        Icon(Icons.Filled.History, contentDescription = "Abrir historial")
                    }
                    IconButton(onClick = onIrAVecinos) {
                        Icon(Icons.Filled.People, contentDescription = "Abrir vecinos")
                    }
                    IconButton(onClick = {
                        authViewModel.cerrarSesion()
                        onCerrarSesion()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            PanelScreen(
                panel = panel,
                nombreVecino = usuario?.nombre ?: "Vecino demo",
                onTipoChange = alarmaViewModel::onTipoChange,
                onDescripcionChange = alarmaViewModel::onDescripcionChange,
                onTomarFoto = {},
                onPedirUbicacion = {},
                onActivar = {
                    alarmaViewModel.activarAlarma(usuario?.nombre ?: "Vecino demo")
                },
                onDesactivar = alarmaViewModel::desactivarAlarma
            )
        }
    }
}
