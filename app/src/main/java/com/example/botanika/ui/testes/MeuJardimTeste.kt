package com.example.botanika.ui.testes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botanika.data.remote.PlantasService
import com.example.botanika.data.remote.Planta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MeuJardimTeste(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plantasService = remember { PlantasService() }
    val scope = rememberCoroutineScope()

    var listaJardim by remember { mutableStateOf<List<Planta>>(emptyList()) }
    var mensagem by remember { mutableStateOf("") }

    fun carregarJardim() {
        scope.launch {
            val plantas = withContext(Dispatchers.IO) {
                plantasService.verPlantasDoUsuario()
            }
            if (plantas != null) {
                listaJardim = plantas
                mensagem = "Plantas carregadas: ${plantas.size}"
            } else {
                mensagem = "Erro ao carregar jardim ou usuário não logado."
            }
        }
    }

    LaunchedEffect(Unit) {
        carregarJardim()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onVoltar) {
            Text("Voltar / Menu")
        }

        Text("Meu Jardim - Gerenciar Plantas", style = MaterialTheme.typography.titleLarge)

        Button(onClick = { carregarJardim() }, modifier = Modifier.fillMaxWidth()) {
            Text("Atualizar Jardim")
        }

        if (mensagem.isNotEmpty()) {
            Text(text = mensagem, style = MaterialTheme.typography.bodyMedium)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaJardim) { planta ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = planta.nomeComum, style = MaterialTheme.typography.titleMedium)
                        Text(text = planta.nomeCientifico, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "ID: ${planta.id}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val sucesso = withContext(Dispatchers.IO) {
                                        plantasService.removerPlantaDoJardim(planta.id)
                                    }
                                    if (sucesso) {
                                        mensagem = "Planta '${planta.nomeComum}' removida do jardim!"
                                        carregarJardim()
                                    } else {
                                        mensagem = "Erro ao remover planta."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Remover do Jardim")
                        }
                    }
                }
            }
        }
    }
}
