package com.example.alarmacomunitariazaror.model

// Enum = lista cerrada de opciones. Cada una lleva el texto que vera el vecino.
enum class TipoEmergencia(val etiqueta: String) {
    ROBO("Robo o intento de robo"),
    SOSPECHOSO("Persona sospechosa"),
    INCENDIO("Incendio"),
    MEDICA("Emergencia médica"),
    FALLA_SISTEMA("Falla del sistema de alarma"), // el caso pide detectar fallas del sistema
    OTRO("Otro")
}
