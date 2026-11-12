package com.example.botanika.ui.testes

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botanika.data.remote.UsuarioService
import kotlinx.coroutines.launch

@Composable
fun LoginTeste(
    onVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usuarioService = remember { UsuarioService() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }
    var usuarioAtual by remember { mutableStateOf(usuarioService.usuarioAtual()?.email ?: "Nenhum usuário logado") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onVoltar) {
            Text("Voltar / Menu")
        }

        Text("Teste de Login", style = MaterialTheme.typography.titleLarge)
        Text("Usuário atual: $usuarioAtual", style = MaterialTheme.typography.bodyMedium)

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
                    val sucesso = usuarioService.fazerLogin(email, senha)
                    if (sucesso) {
                        mensagem = "Login realizado com sucesso!"
                        usuarioAtual = usuarioService.usuarioAtual()?.email ?: "Logado"
                    } else {
                        mensagem = "Erro no login."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fazer Login")
        }

        Button(
            onClick = {
                usuarioService.sair()
                usuarioAtual = "Nenhum usuário logado"
                mensagem = "Logout realizado."
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Sair (Logout)")
        }

        if (mensagem.isNotEmpty()) {
            Text(text = mensagem, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
