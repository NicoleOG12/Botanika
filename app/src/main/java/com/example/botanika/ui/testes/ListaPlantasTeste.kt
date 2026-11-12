package com.example.botanika.ui.testes

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.botanika.data.remote.PerenualService
import com.example.botanika.data.remote.Planta
import com.example.botanika.data.remote.PlantasService
import com.example.botanika.ui.theme.BotanikaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ListaPlantasTeste(
    modifier: Modifier = Modifier,
    onNavegarParaDetalhes: (Int) -> Unit
) {
    val perenualService = remember { PerenualService() }
    val plantasService = remember { PlantasService() }
    val scope = rememberCoroutineScope()

    var listaPlantas by remember { mutableStateOf<List<Planta>>(emptyList()) }
    var mensagem by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        listaPlantas = withContext(Dispatchers.IO) {
            perenualService.plantas("")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (mensagem.isNotEmpty()) {
            Text(
                text = mensagem,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items = listaPlantas) { planta ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Log.d("PERENUALLOG", "Id Planta: ${planta.id}")
                            onNavegarParaDetalhes(planta.id)
                        }
                ) {
                   Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = planta.nomeComum,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = planta.nomeCientifico,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val sucesso = withContext(Dispatchers.IO) {
                                        plantasService.adicionarPlantaAoJardim(planta)
                                    }
                                    mensagem = if (sucesso) {
                                        "Planta '${planta.nomeComum}' adicionada ao jardim com sucesso!"
                                    } else {
                                        "Planta '${planta.nomeComum}' já está no jardim ou erro."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Adicionar ao Jardim")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InterfaceDeTestePreview() {
    BotanikaTheme {
        ListaPlantasTeste(
            onNavegarParaDetalhes = {}
        )
    }
}
