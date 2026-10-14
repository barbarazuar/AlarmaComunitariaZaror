package com.example.alarmacomunitariazaror.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.alarmacomunitariazaror.nativo.GestorSonido
import com.example.alarmacomunitariazaror.nativo.GestorUbicacion
import com.example.alarmacomunitariazaror.nativo.ManejadorFoto
import com.example.alarmacomunitariazaror.nativo.Notificador
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
    val context = LocalContext.current
    val panel by alarmaViewModel.panel.collectAsState()
    val usuario by authViewModel.usuario.collectAsState()
    val notificador = remember(context) { Notificador(context.applicationContext) }
    val gestorUbicacion = remember(context) { GestorUbicacion(context.applicationContext) }

    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var fotoPendiente by remember { mutableStateOf<android.net.Uri?>(null) }

    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        alarmaViewModel.onFotoCapturada(fotoPendiente.takeIf { guardada })
        if (!guardada) fotoPendiente = null
    }

    fun abrirCamara() {
        runCatching { ManejadorFoto.crearUriTemporal(context) }
            .onSuccess { uri ->
                fotoPendiente = uri
                tomarFoto.launch(uri)
            }
            .onFailure {
                Toast.makeText(context, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
            }
    }

    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    fun obtenerUbicacion() {
        gestorUbicacion.obtener(alarmaViewModel::onUbicacionObtenida)
    }

    val permisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) obtenerUbicacion()
        else alarmaViewModel.onUbicacionObtenida("Permiso de ubicación denegado")
    }

    fun mostrarNotificacion() {
        notificador.mostrarAlerta(
            titulo = "Alerta de demostración",
            detalle = "${panel.tipo?.etiqueta.orEmpty()} · ${panel.ubicacion}"
        )
    }

    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) mostrarNotificacion()
        else Toast.makeText(context, "Alerta guardada sin notificación", Toast.LENGTH_SHORT).show()
    }

    fun notificarSiCorresponde() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            mostrarNotificacion()
        } else {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(Unit) {
        onDispose { GestorSonido.detener() }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar alerta") },
            text = {
                Text("Esta acción guardará una alerta de demostración solo en este dispositivo.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        val guardada = alarmaViewModel.activarAlarma(
                            vecinoNombre = usuario?.nombre ?: "Vecino demo"
                        )
                        if (guardada) {
                            GestorSonido.iniciar(context)
                            notificarSiCorresponde()
                        }
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }

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
                        GestorSonido.detener()
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
                onTomarFoto = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                    ) abrirCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
                },
                onPedirUbicacion = {
                    val tienePermiso = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (tienePermiso) obtenerUbicacion()
                    else permisoUbicacion.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                },
                onActivar = { mostrarConfirmacion = true },
                onDesactivar = {
                    GestorSonido.detener()
                    alarmaViewModel.desactivarAlarma()
                }
            )
        }
    }
}

