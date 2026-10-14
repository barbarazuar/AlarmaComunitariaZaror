package com.example.alarmacomunitariazaror.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alarmacomunitariazaror.ui.components.CampoValidado
import com.example.alarmacomunitariazaror.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onIrARegistro: () -> Unit,     // la navegación se la pasa quien llama (AppNavigation)
    onLoginExitoso: () -> Unit
) {
    // collectAsState convierte el StateFlow en un estado de Compose: si cambia, se redibuja
    val estado by viewModel.login.collectAsState()

    // LaunchedEffect se ejecuta cuando cambia sesionIniciada. Aquí navegamos al panel.
    LaunchedEffect(estado.sesionIniciada) {
        if (estado.sesionIniciada) {
            onLoginExitoso()
            viewModel.loginConsumido()   // limpia el estado para que no navegue dos veces
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // permite scroll si el teclado tapa el contenido
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text("Alarma Comunitaria", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Junta de Vecinos Población Zaror", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        CampoValidado(
            valor = estado.correo,
            onCambio = viewModel::onLoginCorreo,   // referencia a función: equivale a { viewModel.onLoginCorreo(it) }
            etiqueta = "Correo electrónico",
            icono = Icons.Filled.Email,
            error = estado.errorCorreo,
            tipoTeclado = KeyboardType.Email
        )
        Spacer(Modifier.height(8.dp))
        CampoValidado(
            valor = estado.clave,
            onCambio = viewModel::onLoginClave,
            etiqueta = "Contraseña",
            icono = Icons.Filled.Lock,
            error = estado.errorClave,
            esClave = true,
            accion = ImeAction.Done
        )

        // Mensaje general (credenciales incorrectas) con animación de aparición
        AnimatedVisibility(visible = estado.errorGeneral != null) {
            Text(
                text = estado.errorGeneral ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = viewModel::iniciarSesion,
            enabled = !estado.cargando,             // evita pulsar dos veces mientras carga
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar sesión")
            }
        }
        TextButton(onClick = onIrARegistro) { Text("¿No tienes cuenta? Regístrate") }
    }
}
