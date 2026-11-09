package com.example.botanika.ui.testes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.botanika.data.local.AppDatabase
import com.example.botanika.data.local.LembreteEntity
import com.example.botanika.data.local.LembreteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LembretesTeste(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lembreteService = remember { LembreteService() }
    val scope = rememberCoroutineScope()

    var plantaIdText by remember { mutableStateOf("1") }
    var nomePlanta by remember { mutableStateOf("Rosa") }
    var mensagemAlarme by remember { mutableStateOf("Hora de regar a planta!") }
    var listaLembretes by remember { mutableStateOf<List<LembreteEntity>>(emptyList()) }
    var status by remember { mutableStateOf("") }

    fun carregarLembretes() {
        scope.launch(Dispatchers.IO) {
            val dao = AppDatabase.getDatabase(context).lemebreteDAO()
            val lembretes = dao.listarTodosLembretes()
            withContext(Dispatchers.Main) {
                listaLembretes = lembretes
            }
        }
    }

    LaunchedEffect(Unit) {
        carregarLembretes()
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

        Text("Teste de Lembretes / Alarmes", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = plantaIdText,
            onValueChange = { plantaIdText = it },
            label = { Text("ID da Planta") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nomePlanta,
            onValueChange = { nomePlanta = it },
            label = { Text("Nome da Planta") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = mensagemAlarme,
            onValueChange = { mensagemAlarme = it },
            label = { Text("Mensagem do Alarme") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val id = plantaIdText.toIntOrNull() ?: 1
                val tempo = System.currentTimeMillis() + 10000 // 10 segundos no futuro
                lembreteService.agendar(context, id, nomePlanta, tempo, mensagemAlarme)
                status = "Lembrete agendado para daqui a 10 segundos!"
                carregarLembretes()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agendar Lembrete (+10s)")
        }

        if (status.isNotEmpty()) {
            Text(text = status, style = MaterialTheme.typography.bodyMedium)
        }

        Button(onClick = { carregarLembretes() }) {
            Text("Atualizar Lista de Lembretes")
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaLembretes) { lembrete ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Planta: ${lembrete.nomePlanta} (ID: ${lembrete.plantaId})", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Mensagem: ${lembrete.mensagem}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Tempo: ${lembrete.tempoEmMillis}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
