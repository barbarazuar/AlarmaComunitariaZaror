package com.example.alarmacomunitariazaror.util

import com.example.alarmacomunitariazaror.model.TipoEmergencia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorFormularioTest {

    @Test
    fun `descripcion vacia es valida porque es opcional`() {
        assertNull(ValidadorFormulario.descripcion(""))
    }

    @Test
    fun `descripcion no puede superar 280 caracteres`() {
        val error = ValidadorFormulario.descripcion("a".repeat(281))

        assertEquals("Máximo 280 caracteres (llevas 281)", error)
    }

    @Test
    fun `tipo de emergencia es obligatorio`() {
        assertEquals(
            "Selecciona el tipo de emergencia",
            ValidadorFormulario.tipoEmergencia(null)
        )
        assertNull(ValidadorFormulario.tipoEmergencia(TipoEmergencia.INCENDIO))
    }

    @Test
    fun `correo rechaza un formato incompleto`() {
        assertEquals(
            "Correo inválido (ej: nombre@correo.cl)",
            ValidadorFormulario.correo("vecino-demo")
        )
    }
}
