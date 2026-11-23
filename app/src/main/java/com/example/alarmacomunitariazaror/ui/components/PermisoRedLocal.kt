package com.example.alarmacomunitariazaror.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

@Composable
fun PermisoRedLocal() {
    if (Build.VERSION.SDK_INT < 37) return
    val context = LocalContext.current
    val permiso = Manifest.permission.ACCESS_LOCAL_NETWORK
    fun permitido() = ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED
    var concedido by remember { mutableStateOf(permitido()) }
    val solicitar = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        concedido = it
    }
    // Actualiza el aviso si el permiso cambia desde Ajustes mientras la app estaba fuera.
    LifecycleResumeEffect(Unit) {
        concedido = permitido()
        onPauseOrDispose { }
    }
    if (!concedido) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp)) {
            Text("Para conectar con los servicios del proyecto, permite el acceso a la red local. Sin permiso puedes usar el respaldo del teléfono.",
                style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = { solicitar.launch(permiso) }) { Text("Permitir red local") }
        }
    }
}
