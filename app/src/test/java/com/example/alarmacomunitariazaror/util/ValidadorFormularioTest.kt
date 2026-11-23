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
    fun `descripcion de edicion no puede quedar vacia`() {
        assertEquals(
            "La descripción no puede quedar vacía",
            ValidadorFormulario.descripcionEdicion("   ")
        )
    }

    @Test
    fun `descripcion de edicion exige minimo 5 caracteres`() {
        assertEquals(
            "Escribe al menos 5 caracteres",
            ValidadorFormulario.descripcionEdicion("Hola")
        )
    }

    @Test
    fun `descripcion de edicion valida texto correcto`() {
        assertNull(ValidadorFormulario.descripcionEdicion("Se observa portón abierto en pasaje 3"))
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

    @Test
    fun `correo acepta formato valido`() {
        assertNull(ValidadorFormulario.correo("vecino.zaror@duocuc.cl"))
    }

    @Test
    fun `nombre rechaza digitos o nombres muy cortos`() {
        assertEquals("Debe tener al menos 3 caracteres", ValidadorFormulario.nombre("Al"))
        assertEquals("El nombre no puede contener números", ValidadorFormulario.nombre("Juan123"))
        assertNull(ValidadorFormulario.nombre("Cristóbal Conejeros"))
    }

    @Test
    fun `clave exige longitud y presencia de numeros`() {
        assertEquals("Debe tener al menos 6 caracteres", ValidadorFormulario.clave("12345"))
        assertEquals("Debe incluir al menos un número", ValidadorFormulario.clave("abcdef"))
        assertNull(ValidadorFormulario.clave("segura123"))
    }

    @Test
    fun `confirmar clave detecta discrepancias`() {
        assertEquals(
            "Las contraseñas no coinciden",
            ValidadorFormulario.confirmarClave("segura123", "otraClave456")
        )
        assertNull(ValidadorFormulario.confirmarClave("segura123", "segura123"))
    }

    @Test
    fun `direccion exige minimo 5 caracteres`() {
        assertEquals(
            "Escribe calle y número (ej: Los Jazmines 425)",
            ValidadorFormulario.direccion("Casa")
        )
        assertNull(ValidadorFormulario.direccion("Av. Lo Errázuriz 1020"))
    }
}
