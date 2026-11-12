package com.example.botanika.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

enum class AuthErro {
    SenhaCurta, EmailInvalido, EmailEmUso, CredenciaisInvalidas, SemRede, FalhaAoSalvarPerfil, Desconhecido
}

private const val PERFIL_TIMEOUT_MS = 5_000L

class UsuarioService {
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val TAG = "USUARIO_SERVICE_LOG"

    var ultimoErro: AuthErro? = null
        private set

    private fun classificar(e: Exception): AuthErro = when (e) {
        is FirebaseAuthWeakPasswordException -> AuthErro.SenhaCurta
        is FirebaseAuthUserCollisionException -> AuthErro.EmailEmUso
        is FirebaseAuthInvalidUserException -> AuthErro.CredenciaisInvalidas
        is FirebaseAuthInvalidCredentialsException -> AuthErro.CredenciaisInvalidas
        is FirebaseNetworkException -> AuthErro.SemRede
        is FirebaseFirestoreException -> AuthErro.FalhaAoSalvarPerfil
        else -> AuthErro.Desconhecido
    }

    suspend fun criarConta(nome: String, email: String, senha: String): Boolean {
        ultimoErro = null
        if (senha.length < 6) {
            ultimoErro = AuthErro.SenhaCurta
            return false
        }

        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, senha).await()
            val user = authResult.user
            val uid = user?.uid ?: ""

            val nomeLimpo = nome.trim()
            try {
                if (nomeLimpo.isNotBlank()) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nomeLimpo)
                        .build()
                    withTimeoutOrNull(PERFIL_TIMEOUT_MS) { user?.updateProfile(profileUpdates)?.await() }
                }

                val dados = mapOf(
                    "uid" to uid,
                    "nome" to nomeLimpo,
                    "email" to email.trim()
                )
                withTimeoutOrNull(PERFIL_TIMEOUT_MS) { db.collection("usuarios").document(uid).set(dados).await() }
            } catch (e: Exception) {
                Log.e(TAG, "Conta criada, mas não foi possível salvar o perfil: ${e.localizedMessage}", e)
            }

            Log.d(TAG, "Conta criada com sucesso para o email: $email e nome: $nomeLimpo")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao criar conta para $email: ${e.localizedMessage}", e)
            ultimoErro = when (val erro = classificar(e)) {
                AuthErro.CredenciaisInvalidas -> AuthErro.EmailInvalido
                else -> erro
            }
            false
        }
    }

    suspend fun criarConta(email: String, senha: String): Boolean {
        return criarConta("", email, senha)
    }

    suspend fun fazerLogin(email: String, senha: String): Boolean {
        Log.d(TAG, "Tentando fazer login para o email: $email")
        ultimoErro = null
        return try {
            auth.signInWithEmailAndPassword(email, senha).await()
            Log.d(TAG, "Login realizado com sucesso para o email: $email")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao fazer login para $email: ${e.localizedMessage}", e)
            ultimoErro = classificar(e)
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

    fun criadoEmMillis(): Long? = auth.currentUser?.metadata?.creationTimestamp

    suspend fun atualizarNome(nome: String): Boolean {
        val user = auth.currentUser ?: return false
        val nomeLimpo = nome.trim()
        if (nomeLimpo.isBlank()) return false
        return try {
            val update = UserProfileChangeRequest.Builder().setDisplayName(nomeLimpo).build()
            user.updateProfile(update).await()
            withTimeoutOrNull(PERFIL_TIMEOUT_MS) {
                db.collection("usuarios").document(user.uid).set(mapOf("nome" to nomeLimpo), SetOptions.merge()).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar nome: ${e.localizedMessage}", e)
            false
        }
    }

    suspend fun enviarRedefinicaoSenha(email: String): Boolean {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao enviar redefinição de senha: ${e.localizedMessage}", e)
            false
        }
    }

    fun sair() {
        Log.d(TAG, "Realizando logout do usuário atual: ${auth.currentUser?.email ?: "Nenhum"}")
        auth.signOut()
    }
}
