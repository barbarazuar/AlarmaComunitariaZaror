package com.example.alarmacomunitariazaror.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alarmacomunitariazaror.ui.components.CampoValidado
import com.example.alarmacomunitariazaror.viewmodel.AuthViewModel

@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onRegistrado: () -> Unit,
    onVolver: () -> Unit
) {
    val estado by viewModel.registro.collectAsState()

    // Cuando el registro termina bien, avisamos a la navegación y limpiamos el formulario
    LaunchedEffect(estado.registroExitoso) {
        if (estado.registroExitoso) {
            onRegistrado()
            viewModel.registroConsumido()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)   // separación uniforme entre elementos
    ) {
        Text("Crea tu cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Completa tus datos para enviar y recibir alertas del sector. Usa datos ficticios en las pruebas.",
            style = MaterialTheme.typography.bodyMedium
        )

        CampoValidado(estado.nombre, viewModel::onRegNombre, "Nombre completo", Icons.Filled.Person, estado.errores.nombre)
        CampoValidado(
            estado.correo, viewModel::onRegCorreo, "Correo electrónico", Icons.Filled.Email,
            estado.errores.correo, tipoTeclado = KeyboardType.Email
        )
        CampoValidado(estado.direccion, viewModel::onRegDireccion, "Dirección (ej: Los Jazmines 425)", Icons.Filled.Home, estado.errores.direccion)
        CampoValidado(
            estado.clave, viewModel::onRegClave, "Contraseña", Icons.Filled.Lock,
            estado.errores.clave, esClave = true
        )
        CampoValidado(
            estado.confirmarClave, viewModel::onRegConfirmar, "Repetir contraseña", Icons.Filled.Lock,
            estado.errores.confirmar, esClave = true, accion = ImeAction.Done
        )

        // Checkbox de términos + mensaje de error animado
        // toggleable: tocar el texto también marca la casilla (área táctil más grande)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = estado.aceptaTerminos,
                    role = Role.Checkbox,
                    onValueChange = viewModel::onRegTerminos
                )
        ) {
            Checkbox(checked = estado.aceptaTerminos, onCheckedChange = null) // el clic lo maneja la fila
            Text("Acepto los términos y condiciones")
        }
        AnimatedVisibility(visible = estado.errores.terminos != null) {
            Text(
                text = estado.errores.terminos ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = viewModel::registrar,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (estado.cargando) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Registrarme")
            }
        }
        TextButton(onClick = onVolver, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Ya tengo cuenta")
        }
    }
}
