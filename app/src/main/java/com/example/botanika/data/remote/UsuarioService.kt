package com.example.botanika.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class UsuarioService {
        private val auth = Firebase.auth
        private val TAG = "USUARIO_SERVICE_LOG"

        suspend fun criarConta(email: String, senha: String): Boolean {

            if(senha.length < 6){
                return false
            }

            return try {
                auth.createUserWithEmailAndPassword(email, senha).await()
                Log.d(TAG, "Conta criada com sucesso para o email: $email")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao criar conta para $email: ${e.localizedMessage}", e)
                false
            }
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

        fun sair() {
            Log.d(TAG, "Realizando logout do usuário atual: ${auth.currentUser?.email ?: "Nenhum"}")
            auth.signOut()
        }
}
