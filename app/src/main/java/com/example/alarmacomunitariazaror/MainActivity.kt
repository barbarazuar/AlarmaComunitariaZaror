package com.example.alarmacomunitariazaror

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.alarmacomunitariazaror.navigation.AppNavigation
import com.example.alarmacomunitariazaror.ui.theme.AlarmaComunitariaZarorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmaComunitariaZarorTheme {
                AppNavigation()
            }
        }
    }
}
