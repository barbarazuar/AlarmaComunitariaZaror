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
            PantallaSecundaria(
                titulo = Screen.Historial.titulo,
                onVolver = { navController.popBackStack() }
            ) {
                HistorialScreen(alertas)
            }
        }

        composable(Screen.Vecinos.route) {
            PantallaSecundaria(
                titulo = Screen.Vecinos.titulo,
                onVolver = { navController.popBackStack() }
            ) {
                VecinosScreen()
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
