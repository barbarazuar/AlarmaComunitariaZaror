package com.example.alarmacomunitariazaror.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTest {

    private val screens = listOf(
        Screen.Login,
        Screen.Registro,
        Screen.Panel,
        Screen.Historial,
        Screen.Vecinos
    )

    @Test
    fun `cada pantalla tiene una ruta unica`() {
        val routes = screens.map { it.route }

        assertEquals(routes.size, routes.distinct().size)
    }

    @Test
    fun `todas las pantallas tienen titulo visible`() {
        assertTrue(screens.all { it.titulo.isNotBlank() })
    }
}
