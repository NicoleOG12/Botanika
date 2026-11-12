package com.example.botanika.data.remote

import android.util.Log
import com.example.botanika.ui.explore.PlantFilter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class PlantasService {

    private val db = Firebase.firestore

    private val deepLApi = DeepLApi()
    val TAG = "PLANTAS_SERVICE_LOG"

    suspend fun buscarPlantasPerenual(
        busca: String = "",
        filtro: PlantFilter = PlantFilter.All
    ): List<Planta> {
        val termo = busca.trim().lowercase()
        Log.d(TAG, "buscarPlantasPerenual - Buscando na coleção 'plantas_perenual' (query='$busca', filtro=$filtro)")

        return try {
            val querySnapshot = when (filtro) {
                PlantFilter.Indoor -> {
                    // Consulta filtrando por plantas indoor no Firestore
                    val qDetails = db.collection("plantas_perenual")
                        .whereEqualTo("details.indoor", true)
                        .limit(20)
                        .get()
                        .await()

                    if (!qDetails.isEmpty) {
                        qDetails
                    } else {
                        val qRoot = db.collection("plantas_perenual")
                            .whereEqualTo("indoor", true)
                            .limit(20)
                            .get()
                            .await()
                        if (!qRoot.isEmpty) qRoot else db.collection("plantas_perenual").limit(30).get().await()
                    }
                }
                PlantFilter.Sun -> {
                    // Consulta filtrando por plantas de sol no Firestore
                    val qDetailsSun = db.collection("plantas_perenual")
                        .whereArrayContains("details.sunlight", "full sun")
                        .limit(20)
                        .get()
                        .await()

                    if (!qDetailsSun.isEmpty) {
                        qDetailsSun
                    } else {
                        val qRootSun = db.collection("plantas_perenual")
                            .whereArrayContains("sunlight", "full sun")
                            .limit(20)
                            .get()
                            .await()
                        if (!qRootSun.isEmpty) qRootSun else db.collection("plantas_perenual").limit(30).get().await()
                    }
                }
                PlantFilter.All -> {
                    db.collection("plantas_perenual")
                        .limit(30)
                        .get()
                        .await()
                }
            }

            val lista = mutableListOf<Planta>()

            for (doc in querySnapshot.documents) {
                val data = doc.data ?: continue

                val detailsMap = data["details"] as? Map<*, *>

                val isIndoor = (detailsMap?.get("indoor") as? Boolean)
                    ?: (data["indoor"] as? Boolean)
                    ?: false

                val sunlightVal = detailsMap?.get("sunlight") ?: data["sunlight"]
                val isSun = when (sunlightVal) {
                    is List<*> -> sunlightVal.any {
                        val s = it?.toString()?.lowercase() ?: ""
                        s.contains("sun") || s.contains("sol") || s.contains("pleno")
                    }
                    is String -> {
                        val s = sunlightVal.lowercase()
                        s.contains("sun") || s.contains("sol") || s.contains("pleno")
                    }
                    else -> false
                }

                // Filtragem de garantia caso a consulta tenha caído no fallback da coleção
                if (filtro == PlantFilter.Indoor && !isIndoor) continue
                if (filtro == PlantFilter.Sun && !isSun) continue

                val id = (data["id"] as? Number)?.toInt()
                    ?: doc.id.toIntOrNull()
                    ?: 0

                val commonName = data["common_name"] as? String
                    ?: data["nomeComum"] as? String
                    ?: data["commonName"] as? String
                    ?: "Nome desconhecido"

                val scientificVal = data["scientific_name"] ?: data["nomeCientifico"] ?: data["scientificName"]
                val scientificName = when (scientificVal) {
                    is List<*> -> scientificVal.firstOrNull()?.toString() ?: "Científico não informado"
                    is String -> scientificVal
                    else -> "Científico não informado"
                }

                val defaultImage = (data["default_image"] as? Map<*, *>)
                    ?: (data["imagemPadrao"] as? Map<*, *>)
                    ?: (detailsMap?.get("default_image") as? Map<*, *>)
                    ?: (detailsMap?.get("imagemPadrao") as? Map<*, *>)

                val license = (defaultImage?.get("license") as? Number)?.toInt()
                val licenseName = defaultImage?.get("license_name") as? String
                val licenseUrl = defaultImage?.get("license_url") as? String
                val originalUrl = defaultImage?.get("original_url") as? String
                val regularUrl = defaultImage?.get("regular_url") as? String
                    ?: data["regularUrl"] as? String
                    ?: (detailsMap?.get("regular_url") as? String)
                val mediumUrl = defaultImage?.get("medium_url") as? String
                    ?: data["mediumUrl"] as? String
                    ?: (detailsMap?.get("medium_url") as? String)
                val smallUrl = defaultImage?.get("small_url") as? String
                    ?: data["smallUrl"] as? String
                    ?: (detailsMap?.get("small_url") as? String)
                val thumbnail = defaultImage?.get("thumbnail") as? String
                    ?: data["thumbnail"] as? String
                    ?: (detailsMap?.get("thumbnail") as? String)

                lista.add(
                    Planta(
                        id = id,
                        nomeComum = deepLApi.traduzirParaPortugues(commonName),
                        nomeCientifico = scientificName,
                        license = license,
                        licenseName = licenseName,
                        licenseUrl = licenseUrl,
                        originalUrl = originalUrl,
                        regularUrl = regularUrl,
                        mediumUrl = mediumUrl,
                        smallUrl = smallUrl,
                        thumbnail = thumbnail,
                        isIndoor = isIndoor,
                        isSun = isSun
                    )
                )
            }

            val resultado = if (termo.isBlank()) {
                lista
            } else {
                val termoTraduzido = deepLApi.traduzirParaIngles(termo)
                lista.filter { planta ->
                    planta.nomeComum.lowercase().contains(termoTraduzido) ||
                    planta.nomeCientifico.lowercase().contains(termoTraduzido)
                }
            }

            Log.d(TAG, "buscarPlantasPerenual - Encontradas ${resultado.size} plantas (filtro: $filtro)")
            resultado
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar plantas em 'plantas_perenual': ${e.localizedMessage}", e)
            emptyList()
        }
    }

    suspend fun buscarDetalhesPlanta(idPlanta: Int): PlantaDetalhes? {
        return buscarDetalhesPlanta(idPlanta.toString())
    }

    suspend fun buscarDetalhesPlanta(idPlanta: String): PlantaDetalhes? {
        Log.d(TAG, "buscarDetalhesPlanta - Buscando detalhes da planta $idPlanta no Firestore na coleção 'plantas_perenual'")
        return try {
            val idNum = idPlanta.toIntOrNull()

            // 1. Tentar obter direto pelo ID do documento
            var doc = db.collection("plantas_perenual")
                .document(idPlanta)
                .get()
                .await()

            // 2. Se não encontrou e o id for numérico, tentar query pelo campo "id"
            if (!doc.exists() && idNum != null) {
                val queryNum = db.collection("plantas_perenual")
                    .whereEqualTo("id", idNum)
                    .limit(1)
                    .get()
                    .await()
                if (!queryNum.isEmpty) {
                    doc = queryNum.documents.first()
                }
            }

            // 3. Se ainda não encontrou, tentar query pelo campo "id" como String
            if (!doc.exists()) {
                val queryStr = db.collection("plantas_perenual")
                    .whereEqualTo("id", idPlanta)
                    .limit(1)
                    .get()
                    .await()
                if (!queryStr.isEmpty) {
                    doc = queryStr.documents.first()
                }
            }

            // 4. Se ainda não encontrou e idNum != null, tentar pelo campo aninhado "details.id"
            if (!doc.exists() && idNum != null) {
                val queryDetailsNum = db.collection("plantas_perenual")
                    .whereEqualTo("details.id", idNum)
                    .limit(1)
                    .get()
                    .await()
                if (!queryDetailsNum.isEmpty) {
                    doc = queryDetailsNum.documents.first()
                }
            }

            if (!doc.exists()) {
                Log.w(TAG, "buscarDetalhesPlanta - Documento não encontrado para ID: $idPlanta")
                return null
            }

            val data = doc.data ?: return null

            // Os detalhes estão no próprio documento em details{} na mesma estrutura da perenual
            val detailsObj = data["details"] ?: data

            val gson = Gson()
            val detailsJson = when (detailsObj) {
                is String -> detailsObj
                else -> gson.toJson(detailsObj)
            }

            var detalhes: PlantaDetalhes? = try {
                gson.fromJson(detailsJson, PlantaDetalhes::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao converter details para PlantaDetalhes: ${e.message}", e)
                null
            }

            if (detalhes == null) {
                return null
            }

            // Fallback para campos básicos caso não estejam preenchidos no details{}
            val finalId = if (detalhes.id != 0) {
                detalhes.id
            } else {
                (data["id"] as? Number)?.toInt()
                    ?: doc.id.toIntOrNull()
                    ?: idNum
                    ?: 0
            }

            val commonNameDoc = data["common_name"] as? String
                ?: data["nomeComum"] as? String
                ?: data["commonName"] as? String

            val finalNomeComum = if (!detalhes.nomeComum.isNullOrBlank()) {
                detalhes.nomeComum
            } else {
                commonNameDoc
            }

            val finalScientific = if (!detalhes.nomeCientifico.isNullOrEmpty()) {
                detalhes.nomeCientifico
            } else {
                val scVal = data["scientific_name"] ?: data["nomeCientifico"] ?: data["scientificName"]
                when (scVal) {
                    is List<*> -> scVal.mapNotNull { it?.toString() }
                    is String -> listOf(scVal)
                    else -> emptyList()
                }
            }

            val finalImagem = detalhes.imagemPadrao ?: run {
                val defaultImage = data["default_image"] as? Map<*, *>
                    ?: data["imagemPadrao"] as? Map<*, *>
                if (defaultImage != null) {
                    DefaultImageDto(
                        licenca = (defaultImage["license"] as? Number)?.toInt(),
                        nomeLicenca = defaultImage["license_name"] as? String,
                        urlLicenca = defaultImage["license_url"] as? String,
                        urlOriginal = defaultImage["original_url"] as? String,
                        urlRegular = defaultImage["regular_url"] as? String,
                        urlMedia = defaultImage["medium_url"] as? String,
                        urlPequena = defaultImage["small_url"] as? String,
                        miniatura = defaultImage["thumbnail"] as? String
                    )
                } else null
            }

            detalhes = detalhes.copy(
                id = finalId,
                nomeComum = finalNomeComum,
                nomeCientifico = finalScientific,
                imagemPadrao = finalImagem
            )

            // Traduz todas as strings de PlantaDetalhes (menos o nome científico)
            val detalhesTraduzidos = traduzirDetalhes(detalhes)

            Log.d(TAG, "buscarDetalhesPlanta - Sucesso ao carregar e traduzir detalhes da planta $idPlanta: ${detalhesTraduzidos.nomeComum}")
            detalhesTraduzidos
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar detalhes da planta $idPlanta no Firestore: ${e.localizedMessage}", e)
            null
        }
    }

    private fun traduzirDetalhes(detalhes: PlantaDetalhes): PlantaDetalhes {
        return try {
            val stringsParaTraduzir = mutableListOf<String>()

            fun registrar(s: String?): Int {
                if (s.isNullOrBlank()) return -1
                stringsParaTraduzir.add(s)
                return stringsParaTraduzir.size - 1
            }

            fun registrarLista(lista: List<String>?): List<Int> {
                if (lista.isNullOrEmpty()) return emptyList()
                return lista.map { registrar(it) }
            }

            val idxNomeComum = registrar(detalhes.nomeComum)
            // IMPORTANTE: O nome científico NÃO é registrado para tradução!
            val idxOutrosNomes = registrarLista(detalhes.outrosNomes)
            val idxFamilia = registrar(detalhes.familia)
            val idxHibrido = registrar(detalhes.hibrido)
            val idxAutoridade = registrar(detalhes.autoridade)
            val idxSubespecie = registrar(detalhes.subespecie)
            val idxCultivar = registrar(detalhes.cultivar)
            val idxVariedade = registrar(detalhes.variedade)
            val idxEpitetos = registrar(detalhes.epitetosEspecie)
            val idxGenero = registrar(detalhes.genero)
            val idxOrigem = registrarLista(detalhes.origem)
            val idxTipo = registrar(detalhes.tipo)
            val idxCiclo = registrar(detalhes.ciclo)
            val idxAtrai = registrarLista(detalhes.atrai)
            val idxPropagacao = registrarLista(detalhes.propagacao)
            val idxHardinessMin = registrar(detalhes.resistenciaFrio?.min)
            val idxHardinessMax = registrar(detalhes.resistenciaFrio?.max)
            val idxRega = registrar(detalhes.rega)
            val idxBenchmarkValor = registrar(detalhes.benchmarkRega?.valor)
            val idxBenchmarkUnidade = registrar(detalhes.benchmarkRega?.unidade)
            val idxAnatomia = detalhes.anatomiaPlanta?.map { item ->
                if (item is String) registrar(item) else -1
            } ?: emptyList()
            val idxLuzSolar = registrarLista(detalhes.luzSolar)
            val idxMesesPoda = registrarLista(detalhes.mesesPoda)
            val idxContagemPoda = registrarLista(detalhes.contagemPoda)
            val idxManutencao = registrar(detalhes.manutencao)
            val idxSolo = registrarLista(detalhes.solo)
            val idxTaxaCrescimento = registrar(detalhes.taxaCrescimento)
            val idxNivelCuidados = registrar(detalhes.nivelCuidados)
            val idxSuscetibilidadePragas = registrarLista(detalhes.suscetibilidadePragas)
            val idxTemporadaFloracao = registrar(detalhes.temporadaFloracao)
            val idxTemporadaColheita = registrar(detalhes.temporadaColheita)
            val idxDescricao = registrar(detalhes.descricao)

            val idxDimensoes = detalhes.dimensoes?.map {
                registrar(it.tipo) to registrar(it.unidade)
            } ?: emptyList()

            if (stringsParaTraduzir.isEmpty()) return detalhes

            val stringsTraduzidas = deepLApi.traduzirListaParaPortugues(stringsParaTraduzir)

            fun obter(idx: Int, original: String?): String? {
                if (idx < 0 || idx >= stringsTraduzidas.size) return original
                val trad = stringsTraduzidas[idx]
                return if (trad.isNotBlank()) trad else original
            }

            fun obterLista(indices: List<Int>, original: List<String>?): List<String>? {
                if (original == null) return null
                return indices.mapIndexed { i, idx -> obter(idx, original.getOrNull(i)) ?: "" }
            }

            val dimensoesTraduzidas = detalhes.dimensoes?.mapIndexed { i, dim ->
                val pair = idxDimensoes.getOrNull(i)
                dim.copy(
                    tipo = if (pair != null) obter(pair.first, dim.tipo) else dim.tipo,
                    unidade = if (pair != null) obter(pair.second, dim.unidade) else dim.unidade
                )
            }

            val hardinessTraduzido = detalhes.resistenciaFrio?.copy(
                min = obter(idxHardinessMin, detalhes.resistenciaFrio.min),
                max = obter(idxHardinessMax, detalhes.resistenciaFrio.max)
            )

            val benchmarkTraduzido = detalhes.benchmarkRega?.copy(
                valor = obter(idxBenchmarkValor, detalhes.benchmarkRega.valor),
                unidade = obter(idxBenchmarkUnidade, detalhes.benchmarkRega.unidade)
            )

            val anatomiaTraduzida = detalhes.anatomiaPlanta?.mapIndexed { i, item ->
                val idx = idxAnatomia.getOrNull(i) ?: -1
                if (item is String && idx >= 0) {
                    obter(idx, item) ?: item
                } else {
                    item
                }
            }

            detalhes.copy(
                nomeComum = obter(idxNomeComum, detalhes.nomeComum),
                // nomeCientifico permanece intacto sem traduzir
                outrosNomes = obterLista(idxOutrosNomes, detalhes.outrosNomes),
                familia = obter(idxFamilia, detalhes.familia),
                hibrido = obter(idxHibrido, detalhes.hibrido),
                autoridade = obter(idxAutoridade, detalhes.autoridade),
                subespecie = obter(idxSubespecie, detalhes.subespecie),
                cultivar = obter(idxCultivar, detalhes.cultivar),
                variedade = obter(idxVariedade, detalhes.variedade),
                epitetosEspecie = obter(idxEpitetos, detalhes.epitetosEspecie),
                genero = obter(idxGenero, detalhes.genero),
                origem = obterLista(idxOrigem, detalhes.origem),
                tipo = obter(idxTipo, detalhes.tipo),
                dimensoes = dimensoesTraduzidas,
                ciclo = obter(idxCiclo, detalhes.ciclo),
                atrai = obterLista(idxAtrai, detalhes.atrai),
                propagacao = obterLista(idxPropagacao, detalhes.propagacao),
                resistenciaFrio = hardinessTraduzido,
                rega = obter(idxRega, detalhes.rega),
                benchmarkRega = benchmarkTraduzido,
                anatomiaPlanta = anatomiaTraduzida,
                luzSolar = obterLista(idxLuzSolar, detalhes.luzSolar),
                mesesPoda = obterLista(idxMesesPoda, detalhes.mesesPoda),
                contagemPoda = obterLista(idxContagemPoda, detalhes.contagemPoda),
                manutencao = obter(idxManutencao, detalhes.manutencao),
                solo = obterLista(idxSolo, detalhes.solo),
                taxaCrescimento = obter(idxTaxaCrescimento, detalhes.taxaCrescimento),
                nivelCuidados = obter(idxNivelCuidados, detalhes.nivelCuidados),
                suscetibilidadePragas = obterLista(idxSuscetibilidadePragas, detalhes.suscetibilidadePragas),
                temporadaFloracao = obter(idxTemporadaFloracao, detalhes.temporadaFloracao),
                temporadaColheita = obter(idxTemporadaColheita, detalhes.temporadaColheita),
                descricao = obter(idxDescricao, detalhes.descricao)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao traduzir detalhes: ${e.message}", e)
            detalhes
        }
    }

    suspend fun adicionarPlantaAoJardim(planta: Planta): Boolean{
        val idUsuario = Firebase.auth.currentUser?.uid ?: ""
        Log.d(TAG, "adicionarPlantaAoJardim - ID Planta: ${planta.id}, Nome: ${planta.nomeComum}, Usuario: $idUsuario")
        try{
            val dados = mutableMapOf<String, Any>(
                "idPlanta" to planta.id,
                "nomePlanta" to deepLApi.traduzirParaPortugues(planta.nomeComum),
                "nomeCientifico" to planta.nomeCientifico,
                "idUsuario" to idUsuario
            )

            val foto = planta.regularUrl ?: planta.mediumUrl ?: planta.originalUrl ?: planta.smallUrl ?: planta.thumbnail
            if (!foto.isNullOrBlank()) dados["thumbnail"] = foto

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
                val scientificVal = doc.get("nomeCientifico")
                val cientifico = when (scientificVal) {
                    is List<*> -> scientificVal.firstOrNull()?.toString() ?: "Científico desconhecido"
                    is String -> scientificVal
                    else -> "Científico desconhecido"
                }
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
