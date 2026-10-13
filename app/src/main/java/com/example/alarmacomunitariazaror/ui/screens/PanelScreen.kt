package com.example.alarmacomunitariazaror.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.alarmacomunitariazaror.model.PanelUiState
import com.example.alarmacomunitariazaror.model.TipoEmergencia
import com.example.alarmacomunitariazaror.ui.components.CampoValidado
import com.example.alarmacomunitariazaror.ui.components.SelectorTipoEmergencia

@Composable
fun PanelScreen(
    panel: PanelUiState,                          // todo lo que la pantalla muestra
    nombreVecino: String,                         // datos del vecino con sesión iniciada
    onTipoChange: (TipoEmergencia) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onTomarFoto: () -> Unit,                      // la acción real (cámara) la decide PanelRoute
    onPedirUbicacion: () -> Unit,
    onActivar: () -> Unit,
    onDesactivar: () -> Unit
) {
    // ----- ANIMACIÓN 1: pulso infinito del botón mientras la alarma está activa -----
    val transicion = rememberInfiniteTransition(label = "pulso")
    val escalaPulso by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,                                  // crece un 8 %
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),          // 0,6 s de ida
            repeatMode = RepeatMode.Reverse                   // y vuelve
        ),
        label = "escala"
    )
    val escala = if (panel.alarmaActiva) escalaPulso else 1f  // sin alarma: tamaño normal

    // ----- ANIMACIÓN 2: el color cambia suavemente al activar/detener -----
    val colorBoton by animateColorAsState(
        targetValue = if (panel.alarmaActiva) Color(0xFF7F0000) else Color(0xFFD32F2F),
        animationSpec = tween(500),
        label = "colorBoton"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Tarjeta con los datos del vecino ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(nombreVecino, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Sector Población Zaror, Cerrillos", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // --- ANIMACIÓN 3: aviso verde que aparece y desaparece ---
        AnimatedVisibility(
            visible = panel.mensajeExito,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("¡Alerta enviada a los vecinos del sector!", color = Color.White)
                }
            }
        }

        // --- Formulario de la alerta (con validación por campo) ---
        SelectorTipoEmergencia(panel.tipo, onTipoChange, panel.errores.tipo)
        CampoValidado(
            valor = panel.descripcion,
            onCambio = onDescripcionChange,
            etiqueta = "¿Qué está ocurriendo?",
            icono = Icons.Filled.Edit,
            error = panel.errores.descripcion,
            accion = ImeAction.Done
        )

        // --- Botones de recursos nativos (opcionales) ---
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPedirUbicacion, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Ubicación")
            }
            OutlinedButton(onClick = onTomarFoto, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Foto")
            }
        }
        Text("Ubicación: ${panel.ubicacion}", style = MaterialTheme.typography.bodySmall)

        // Vista previa de la foto (solo aparece si hay una)
        AnimatedVisibility(visible = panel.fotoUri != null) {
            AsyncImage(
                model = panel.fotoUri,
                contentDescription = "Foto adjunta a la alerta",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        Spacer(Modifier.height(8.dp))

        // --- BOTÓN DE PÁNICO ---
        Button(
            // Si la alarma suena, el botón la detiene; si no, intenta activarla
            onClick = if (panel.alarmaActiva) onDesactivar else onActivar,
            colors = ButtonDefaults.buttonColors(containerColor = colorBoton, contentColor = Color.White),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(200.dp).scale(escala)   // scale aplica el pulso
        ) {
            Text(
                text = if (panel.alarmaActiva) "DETENER\nALARMA" else "ACTIVAR\nALARMA",
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = if (panel.alarmaActiva) "La alarma está sonando…" else "Completa los datos y pulsa el botón",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
