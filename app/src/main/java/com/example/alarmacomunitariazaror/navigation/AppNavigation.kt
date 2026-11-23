package com.example.alarmacomunitariazaror.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmacomunitariazaror.ui.screens.HistorialScreen
import com.example.alarmacomunitariazaror.ui.screens.LoginScreen
import com.example.alarmacomunitariazaror.ui.screens.RegistroScreen
import com.example.alarmacomunitariazaror.ui.screens.VecinosScreen
import com.example.alarmacomunitariazaror.viewmodel.AlarmaViewModel
import com.example.alarmacomunitariazaror.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val alarmaViewModel: AlarmaViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onIrARegistro = {
                    navController.navigate(Screen.Registro.route) {
                        launchSingleTop = true
                    }
                },
                onLoginExitoso = {
                    navController.navigate(Screen.Panel.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                viewModel = authViewModel,
                onRegistrado = { navController.popBackStack() },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Screen.Panel.route) {
            PanelRoute(
                authViewModel = authViewModel,
                alarmaViewModel = alarmaViewModel,
                onIrAHistorial = { navController.navigate(Screen.Historial.route) },
                onIrAVecinos = { navController.navigate(Screen.Vecinos.route) },
                onCerrarSesion = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Historial.route) {
            val alertas by alarmaViewModel.alertas.collectAsState()
            val remoto by alarmaViewModel.remoto.collectAsState()
            // Al abrir el historial se sincroniza con ms-alertas
            LaunchedEffect(Unit) { alarmaViewModel.refrescarAlertas() }
            PantallaSecundaria(
                titulo = Screen.Historial.titulo,
                onVolver = { navController.popBackStack() }
            ) {
                HistorialScreen(
                    alertas = alertas,
                    refrescando = remoto.refrescando,
                    mensaje = remoto.mensaje,
                    onRefrescar = alarmaViewModel::refrescarAlertas,
                    onAtender = alarmaViewModel::marcarAtendida,
                    onEliminar = alarmaViewModel::eliminarAlerta
                )
            }
        }

        composable(Screen.Vecinos.route) {
            val vecinos by alarmaViewModel.vecinos.collectAsState()
            LaunchedEffect(Unit) { alarmaViewModel.cargarVecinos() }
            PantallaSecundaria(
                titulo = Screen.Vecinos.titulo,
                onVolver = { navController.popBackStack() }
            ) {
                VecinosScreen(vecinos)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaSecundaria(
    titulo: String,
    onVolver: () -> Unit,
    contenido: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            contenido()
        }
    }
}
