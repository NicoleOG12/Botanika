package com.example.botanika.data.remote

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject

class PerenualService {
    private val perenualApi: PerenualApi = PerenualApi()

    fun plantas(busca: String): List<Planta> {
        val plantasList = mutableListOf<Planta>()

        val res: String? = perenualApi.buscarPlantas(busca)
        if (res.isNullOrBlank()) return emptyList()

        val jsonObject = JSONObject(res)
        val dataArray = jsonObject.optJSONArray("data") ?: return emptyList()

        for (i in 0 until dataArray.length()) {
            val item = dataArray.getJSONObject(i)
            Log.d("PERENUALLOG", "Item JSON: $item")

            val id = item.optInt("id", 0)
            val commonName = item.optString("common_name", "Nome desconhecido")
            Log.d("PERENUALLOG", "Planta parseada - id: $id, nome: $commonName")

            val scientificArray = item.optJSONArray("scientific_name")
            val scientificName = if (scientificArray != null && scientificArray.length() > 0) {
                scientificArray.getString(0)
            } else {
                "Científico não informado"
            }

            // Extrai o objeto de imagem e pega tudo direto
            val defaultImage = item.optJSONObject("default_image")

            val license = defaultImage?.optInt("license")?.takeIf { defaultImage.has("license") }
            val licenseName = defaultImage?.optString("license_name")
            val licenseUrl = defaultImage?.optString("license_url")
            val originalUrl = defaultImage?.optString("original_url")
            val regularUrl = defaultImage?.optString("regular_url")
            val mediumUrl = defaultImage?.optString("medium_url")
            val smallUrl = defaultImage?.optString("small_url")
            val thumbnail = defaultImage?.optString("thumbnail")

            plantasList.add(
                Planta(
                    id = id,
                    nomeComum = commonName,
                    nomeCientifico = scientificName,
                    license = license,
                    licenseName = licenseName,
                    licenseUrl = licenseUrl,
                    originalUrl = originalUrl,
                    regularUrl = regularUrl,
                    mediumUrl = mediumUrl,
                    smallUrl = smallUrl,
                    thumbnail = thumbnail
                )
            )
        }

        for (planta in plantasList) {
            Log.d("PERENUALLOG", "Planta: ${planta.nomeComum} | Regular: ${planta.regularUrl}")
        }

        return plantasList
    }


    fun detalhes(idPlanta: Int): PlantaDetalhes? {
        Log.d("PERENUALLOG", "id: ${idPlanta}")
        val res = perenualApi.buscarDetalhesDaPlanta(idPlanta)
        if (res.isNullOrBlank()) return null
        return Gson().fromJson(res, PlantaDetalhes::class.java)
    }


}

data class Planta(
    val id: Int,
    val nomeComum: String,
    val nomeCientifico: String,
    val license: Int?,
    val licenseName: String?,
    val licenseUrl: String?,
    val originalUrl: String?,
    val regularUrl: String?,
    val mediumUrl: String?,
    val smallUrl: String?,
    val thumbnail: String?
)

