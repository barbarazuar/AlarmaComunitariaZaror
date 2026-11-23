package com.example.alarmacomunitariazaror.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.alarmacomunitariazaror.model.Alerta
import com.example.alarmacomunitariazaror.ui.screens.HistorialScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// Guia 15, partes 5 y 6: prueba de interfaz con Compose UI Test.
// Se ejecuta en emulador o dispositivo (Run Tests de Android Studio).
class HistorialScreenTest {
    @get:Rule val composeRule = createComposeRule()

    private val alerta = Alerta(
        vecinoNombre = "Ana", ubicacion = "Calle 1", fechaHora = 1000,
        tipoEmergencia = "Incendio", descripcion = "Humo en el pasaje"
    )

    @Test
    fun sinAlertasMuestraElMensajeVacio() {
        composeRule.setContent { HistorialScreen(alertas = emptyList()) }

        composeRule.onNodeWithText("Aún no hay alertas registradas").assertIsDisplayed()
    }

    @Test
    fun conAlertasMuestraTipoYDescripcion() {
        composeRule.setContent { HistorialScreen(alertas = listOf(alerta)) }

        composeRule.onNodeWithText("Incendio").assertIsDisplayed()
        composeRule.onNodeWithText("Humo en el pasaje").assertIsDisplayed()
    }

    @Test
    fun marcarAtendidaAvisaAlViewModel() {
        var atendida: Alerta? = null
        composeRule.setContent {
            HistorialScreen(alertas = listOf(alerta), onAtender = { atendida = it })
        }

        composeRule.onNodeWithText("Marcar atendida").performClick()

        assertEquals(alerta, atendida)
    }

    @Test
    fun eliminarPideConfirmacionAntesDeBorrar() {
        var eliminada: Alerta? = null
        composeRule.setContent {
            HistorialScreen(alertas = listOf(alerta), onEliminar = { eliminada = it })
        }

        composeRule.onNodeWithText("Eliminar").performClick()
        composeRule.onNodeWithText("Confirmar eliminación").assertIsDisplayed()
        assertEquals(null, eliminada)

        composeRule.onNodeWithText("¿Deseas eliminar esta alerta del historial? Esta acción no se puede deshacer.")
            .assertIsDisplayed()
    }
}
