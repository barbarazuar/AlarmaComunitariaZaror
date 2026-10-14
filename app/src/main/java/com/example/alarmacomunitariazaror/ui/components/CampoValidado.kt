package com.example.alarmacomunitariazaror.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

// Campo de texto con: ícono a la izquierda, ícono de error a la derecha y mensaje bajo el campo.
// No contiene reglas de validación: solo recibe el mensaje de error ya calculado (error).
@Composable
fun CampoValidado(
    valor: String,                       // texto actual (viene del ViewModel)
    onCambio: (String) -> Unit,          // avisa al ViewModel cuando el usuario escribe
    etiqueta: String,                    // texto flotante: "Correo electrónico"
    icono: ImageVector,                  // ícono decorativo a la izquierda
    error: String?,                      // null = sin error; texto = mensaje a mostrar
    modifier: Modifier = Modifier,
    esClave: Boolean = false,            // true = oculta el texto con puntos
    tipoTeclado: KeyboardType = KeyboardType.Text,
    accion: ImeAction = ImeAction.Next   // botón del teclado: Next (siguiente) o Done (listo)
) {
    // Recuerda si la contraseña se ve o no (sobrevive a girar la pantalla)
    var claveVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            when {
                // Si hay error: ícono rojo de advertencia
                error != null -> Icon(
                    Icons.Filled.Error,
                    contentDescription = "Campo con error",
                    tint = MaterialTheme.colorScheme.error
                )
                // Si es contraseña: botón de ojo para mostrar/ocultar
                esClave -> IconButton(onClick = { claveVisible = !claveVisible }) {
                    Icon(
                        if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
                else -> Unit
            }
        },
        isError = error != null,                                   // borde rojo
        supportingText = { if (error != null) Text(error) },       // mensaje debajo del campo
        singleLine = true,
        visualTransformation = if (esClave && !claveVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (esClave) KeyboardType.Password else tipoTeclado,
            imeAction = accion
        ),
        modifier = modifier.fillMaxWidth()
    )
}
