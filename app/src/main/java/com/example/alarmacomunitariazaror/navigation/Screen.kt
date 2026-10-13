package com.example.alarmacomunitariazaror.navigation

// sealed class = conjunto cerrado de pantallas. Cada una guarda su ruta y su titulo.
// Asi se evitan errores de tipeo al navegar (se usa Screen.Login.route en vez de "login").
sealed class Screen(val route: String, val titulo: String) {
    data object Login : Screen("login", "Iniciar sesión")
    data object Registro : Screen("registro", "Crear cuenta")
    data object Panel : Screen("panel", "Alarma")
    data object Historial : Screen("historial", "Historial")
    data object Vecinos : Screen("vecinos", "Vecinos")
}
