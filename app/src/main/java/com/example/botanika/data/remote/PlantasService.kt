package com.example.botanika.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class PlantasService {

    private val db = Firebase.firestore
    val TAG = "PLANTAS_SERVICE_LOG"

    suspend fun adicionarPlantaAoJardim(planta: Planta): Boolean{
        val idUsuario = Firebase.auth.currentUser?.uid ?: ""
        Log.d(TAG, "adicionarPlantaAoJardim - ID Planta: ${planta.id}, Nome: ${planta.nomeComum}, Usuario: $idUsuario")
        try{
            val dados = mutableMapOf<String, Any>(
                "idPlanta" to planta.id,
                "nomePlanta" to planta.nomeComum,
                "nomeCientifico" to planta.nomeCientifico,
                "idUsuario" to idUsuario
            )

            if(!planta.smallUrl.isNullOrBlank()) dados["thumbnail"] = planta.smallUrl

            if(temNoJardim(dados)){
                Log.d(TAG, "Planta ${planta.id} já existe no jardim do usuário $idUsuario")
                return false
            }else {
                db.collection("plantasXusuario")
                    .add(dados)
                    .await()
                Log.d(TAG, "Planta ${planta.id} adicionada com sucesso ao jardim!")
            }
            return true

        }catch (e: Exception){
            Log.e(TAG,"Erro ao adicionar planta ao jardim: ${e.localizedMessage}", e)
            return false
        }
    }

    suspend fun removerPlantaDoJardim(idPlanta: Int): Boolean {
        val idUsuario = Firebase.auth.currentUser?.uid ?: ""
        Log.d(TAG, "removerPlantaDoJardim - ID Planta: $idPlanta, Usuario: $idUsuario")
        try {
            val querySnapshot = db.collection("plantasXusuario")
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("idPlanta", idPlanta)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                Log.d(TAG, "Planta $idPlanta não encontrada no jardim do usuário $idUsuario")
                return false
            }

            for (document in querySnapshot.documents) {
                db.collection("plantasXusuario").document(document.id).delete().await()
                Log.d(TAG, "Planta $idPlanta removida com sucesso do jardim!")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao remover planta do jardim: ${e.localizedMessage}", e)
            return false
        }
    }

    suspend fun verPlantasDoUsuario():List<Planta>?{
        val idUsuario = Firebase.auth.currentUser?.uid ?: ""
        Log.d(TAG, "verPlantasDoUsuario - Buscando plantas para o usuario: $idUsuario")
        try{
            val querySnapshot = db.collection("plantasXusuario")
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .await()

            val listaPlantas = mutableListOf<Planta>()
            for (doc in querySnapshot.documents) {
                val id = doc.getLong("idPlanta")?.toInt() ?: doc.getLong("id")?.toInt() ?: 0
                val nome = doc.getString("nomePlanta") ?: doc.getString("nomeComum") ?: "Desconhecido"
                val cientifico = doc.getString("nomeCientifico") ?: "Científico desconhecido"
                val thumbnail = doc.getString("thumbnail")
                listaPlantas.add(
                    Planta(
                        id = id,
                        nomeComum = nome,
                        nomeCientifico = cientifico,
                        license = null,
                        licenseName = null,
                        licenseUrl = null,
                        originalUrl = null,
                        regularUrl = null,
                        mediumUrl = null,
                        smallUrl = thumbnail,
                        thumbnail = thumbnail
                    )
                )
            }
            Log.d(TAG, "verPlantasDoUsuario - Encontradas ${listaPlantas.size} plantas.")
            return listaPlantas

        }catch(e: Exception){
            Log.e(TAG,"Erro ao buscar plantas do usuário: ${e.localizedMessage}", e)
            return null
        }
    }

    suspend fun temNoJardim(dados: Map<String, Any>): Boolean {
        val idUsuario = dados["idUsuario"] ?: return false
        val idPlanta = dados["idPlanta"]
        Log.d(TAG, "temNoJardim - Verificando se planta $idPlanta está no jardim do usuário $idUsuario")
        return try {
            val querySnapshot = db.collection("plantasXusuario")
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("idPlanta", idPlanta)
                .get()
                .await()

            val existe = !querySnapshot.isEmpty
            Log.d(TAG, "temNoJardim - Resultado: $existe")
            existe
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao verificar no jardim: ${e.localizedMessage}", e)
            false
        }
    }

}
