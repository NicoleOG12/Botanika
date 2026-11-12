package com.example.botanika.ui.testes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botanika.data.remote.UsuarioService
import kotlinx.coroutines.launch

@Composable
fun CadastroTeste(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usuarioService = remember { UsuarioService() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onVoltar) {
            Text("Voltar / Menu")
        }

        Text("Teste de Cadastro", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    val sucesso = usuarioService.criarConta(email, senha)
                    mensagem = if (sucesso) "Conta criada com sucesso!" else "Erro ao criar conta."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar Conta")
        }

        if (mensagem.isNotEmpty()) {
            Text(text = mensagem, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
