package com.example.alarmacomunitariazaror.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ClimaTest {
    @Test
    fun `codigos WMO se traducen a texto`() {
        assertEquals("Despejado", descripcionClima(0))
        assertEquals("Nublado", descripcionClima(3))
        assertEquals("Lluvia", descripcionClima(63))
        assertEquals("Tormenta eléctrica", descripcionClima(95))
    }

    @Test
    fun `codigo desconocido no rompe`() {
        assertEquals("Sin datos", descripcionClima(999))
    }
}
