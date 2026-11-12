package com.example.botanika.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UsuarioService {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val TAG = "USUARIO_SERVICE_LOG"

    suspend fun criarConta(nome: String, email: String, senha: String): Boolean {
        if (senha.length < 6) {
            return false
        }

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, senha).await()
            val user = authResult.user
            val uid = user?.uid ?: ""

            val nomeLimpo = nome.trim()
            if (nomeLimpo.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nomeLimpo)
                    .build()
                user?.updateProfile(profileUpdates)?.await()
            }

            val dados = mapOf(
                "uid" to uid,
                "nome" to nomeLimpo,
                "email" to email.trim()
            )
            db.collection("usuarios").document(uid).set(dados).await()

            Log.d(TAG, "Conta criada com sucesso para o email: $email e nome: $nomeLimpo")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar conta para $email: ${e.localizedMessage}", e)
            false
        }
    }

    suspend fun criarConta(email: String, senha: String): Boolean {
        return criarConta("", email, senha)
    }

    suspend fun fazerLogin(email: String, senha: String): Boolean {
        Log.d(TAG, "Tentando fazer login para o email: $email")
        return try {
            auth.signInWithEmailAndPassword(email, senha).await()
            Log.d(TAG, "Login realizado com sucesso para o email: $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer login para $email: ${e.localizedMessage}", e)
            false
        }
    }

    fun usuarioAtual(): FirebaseUser? {
        val user = auth.currentUser
        Log.d(TAG, "Consultando usuário atual: ${user?.email ?: "Nenhum usuário logado"}")
        return user
    }

    suspend fun obterNomeUsuario(): String {
        val user = auth.currentUser ?: return "Usuário"
        val display = user.displayName
        if (!display.isNullOrBlank()) {
            return display
        }
        return try {
            val doc = db.collection("usuarios").document(user.uid).get().await()
            val nome = doc.getString("nome")
            if (!nome.isNullOrBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build()
                user.updateProfile(profileUpdates).await()
                nome
            } else {
                user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Usuário"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao obter nome do usuário: ${e.message}")
            user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() } ?: "Usuário"
        }
    }

    fun sair() {
        Log.d(TAG, "Realizando logout do usuário atual: ${auth.currentUser?.email ?: "Nenhum"}")
        auth.signOut()
    }
}
