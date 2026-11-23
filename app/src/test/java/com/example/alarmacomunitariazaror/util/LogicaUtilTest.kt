package com.example.alarmacomunitariazaror.util

import com.example.alarmacomunitariazaror.model.*
import org.junit.Assert.*
import org.junit.Test
import java.util.TimeZone

class LogicaUtilTest {
    @Test fun `nombre comprueba vacio longitud numeros y caso correcto`() {
        listOf("", "  ", "Al", "Ana1").forEach { assertNotNull(ValidadorFormulario.nombre(it)) }
        assertNull(ValidadorFormulario.nombre(" Ana Pérez "))
    }
    @Test fun `correo comprueba obligatoriedad formato y espacios externos`() {
        assertNotNull(ValidadorFormulario.correo("")); assertNotNull(ValidadorFormulario.correo("ana@"))
        assertNull(ValidadorFormulario.correo(" ana@demo.cl "))
    }
    @Test fun `clave comprueba vacio longitud y digito obligatorio`() {
        listOf("", "abc1", "abcdef").forEach { assertNotNull(ValidadorFormulario.clave(it)) }
        assertNull(ValidadorFormulario.clave("clave1"))
        assertNotNull(ValidadorFormulario.confirmarClave("clave1", ""))
        assertNotNull(ValidadorFormulario.confirmarClave("clave1", "clave2"))
        assertNull(ValidadorFormulario.confirmarClave("clave1", "clave1"))
    }
    @Test fun `direccion y descripcion comprueban sus limites`() {
        assertNotNull(ValidadorFormulario.direccion("")); assertNotNull(ValidadorFormulario.direccion("abc"))
        assertNull(ValidadorFormulario.direccion("Calle 123"))
        assertNotNull(ValidadorFormulario.descripcion("abcd")); assertNull(ValidadorFormulario.descripcion("abcde"))
        assertNull(ValidadorFormulario.descripcion("a".repeat(280)))
        assertNotNull(ValidadorFormulario.descripcion("a".repeat(281)))
        assertNull(ValidadorFormulario.descripcion("   "))
    }
    @Test fun `errores de formularios detectan cada campo y ausencia de errores`() {
        assertFalse(RegistroErrores().hayErrores())
        listOf(RegistroErrores(nombre="x"), RegistroErrores(correo="x"), RegistroErrores(clave="x"),
            RegistroErrores(confirmar="x"), RegistroErrores(direccion="x"), RegistroErrores(terminos="x"))
            .forEach { assertTrue(it.hayErrores()) }
        assertFalse(AlertaErrores().hayErrores()); assertTrue(AlertaErrores(tipo="x").hayErrores())
        assertTrue(AlertaErrores(descripcion="x").hayErrores())
    }
    @Test fun `sha256 coincide con vector conocido y no guarda texto original`() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", "abc".sha256())
        assertEquals(64, "".sha256().length); assertNotEquals("clave1".sha256(), "clave2".sha256())
    }
    @Test fun `fecha formatea milisegundos con dia mes anio y hora`() {
        val anterior = TimeZone.getDefault()
        try { TimeZone.setDefault(TimeZone.getTimeZone("UTC")); assertEquals("01/01/1970 00:00", 0L.comoFechaLegible()) }
        finally { TimeZone.setDefault(anterior) }
    }
    @Test fun `clima interpreta todas las familias de codigos`() {
        val casos = mapOf(0 to "Despejado", 1 to "Mayormente despejado", 2 to "Mayormente despejado",
            3 to "Nublado", 45 to "Niebla", 48 to "Niebla", 51 to "Llovizna", 57 to "Llovizna",
            61 to "Lluvia", 67 to "Lluvia", 71 to "Nieve", 77 to "Nieve", 80 to "Chubascos",
            82 to "Chubascos", 85 to "Nevazón", 86 to "Nevazón", 95 to "Tormenta eléctrica", 99 to "Tormenta eléctrica", -1 to "Sin datos")
        casos.forEach { (codigo, texto) -> assertEquals(texto, descripcionClima(codigo)) }
    }
}
