package com.example.botanika.ui.testes

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavegacao (modifier: Modifier = Modifier){
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "menu",
        modifier = modifier
    ){
        composable("menu") {
            MenuTestes(
                onNavegar = { rota -> navController.navigate(rota) }
            )
        }

        composable("listaPlantas"){
            ListaPlantasTeste(
                modifier = Modifier,
                onNavegarParaDetalhes = { idPLanta ->
                    Log.d("PERENUALLOG", "Navegando para detalhes com idPlanta: $idPLanta")
                    navController.navigate("telaDetalhes/$idPLanta")
                }
            )
        }

        composable ("telaDetalhes/{idPlanta}") { backStackEntry ->
            val idPlantaString = backStackEntry.arguments?.getString("idPlanta")
                ?: backStackEntry.savedStateHandle.get<String>("idPlanta")

            Log.d("PERENUALLOG","idPlantaString: $idPlantaString, savedStateHandle: ${backStackEntry.savedStateHandle.keys()}, arguments: ${backStackEntry.arguments}")

            val idPlantaInt = idPlantaString?.toIntOrNull()
                ?: backStackEntry.savedStateHandle.get<Int>("idPlanta")
                ?: 0

            Log.d("PERENUALLOG", "idPlantaInt final: $idPlantaInt")

            DetalhesPlantaTeste(
                idPlanta = idPlantaInt,
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("cadastro") {
            CadastroTeste(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("login") {
            LoginTeste(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("meuJardim") {
            MeuJardimTeste(
                onVoltar = { navController.popBackStack() }
            )
        }

        composable("lembretes") {
            LembretesTeste(
                onVoltar = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MenuTestes(
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Menu de Testes - Botanika", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { onNavegar("listaPlantas") }, modifier = Modifier.fillMaxWidth()) {
            Text("Testar Busca de Plantas (Perenual)")
        }
        Button(onClick = { onNavegar("cadastro") }, modifier = Modifier.fillMaxWidth()) {
            Text("Testar Cadastro (Firebase Auth)")
        }
        Button(onClick = { onNavegar("login") }, modifier = Modifier.fillMaxWidth()) {
            Text("Testar Login (Firebase Auth)")
        }
        Button(onClick = { onNavegar("meuJardim") }, modifier = Modifier.fillMaxWidth()) {
            Text("Testar Meu Jardim (Firestore)")
        }
        Button(onClick = { onNavegar("lembretes") }, modifier = Modifier.fillMaxWidth()) {
            Text("Testar Lembretes (Room + AlarmManager)")
        }
    }
}
