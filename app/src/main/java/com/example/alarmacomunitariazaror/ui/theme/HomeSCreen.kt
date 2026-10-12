package com.example.alarmacomunitariazaror.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// estructura visual basica
fun HomeScreen() {
    Scaffold(
        topBar ={
            TopAppBar(Litle = { Text("Alarma Comunitaria") })
        }
    ){ innerPadding ->
        //column organiza os elementos visuales de forma vertical
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()// ocupa todo el espacio disponible
                .padding(15.dp)// margen interno alrededor de la columna
                    verticalArrangament = Arrangement.spacedBy(20.dp)
        ){
            Text( text-"Bienvenidos Villa Zaror")
            Button(unClick = {/*Accion futura*/}){
                Text("presionarme")
            }
            image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo App",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen()
}