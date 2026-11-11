package com.example.botanika.ui.plants

import com.example.botanika.R
import com.example.botanika.data.remote.PlantaDetalhes

data class PlantInfo(
    val id: String,
    val name: String?,
    val tags: Int?,
    val image: String?,
    val scientific: String,
    val description: String?,
    val light: String,
    val water: String,
    val temperature: String,
    val difficulty: String?,
    val petSafe: Boolean?,
    val wateringEveryDays: Int?,
    val tips: List<String>?
)

fun PlantaDetalhes.toPlantInfo(): PlantInfo = PlantInfo(
    id = id.toString(),
    name = nomeComum,
    tags = null,
    image = imagemPadrao?.urlRegular ?: imagemPadrao?.miniatura,
    scientific = nomeCientifico.firstOrNull() ?: "",
    description = descricao,
    light = luzSolar.firstOrNull() ?: "",
    water = rega ?: "",
    temperature = temporadaColheita ?: "",
    difficulty = nivelCuidados,
    petSafe = !venenosaPets,
    wateringEveryDays = null,
    tips = null
)

data class PlantMatch(val plant: PlantInfo, val confidence: Int)

object PlantGuides {
    val all: List<PlantInfo> = listOf(

    )

    fun find(id: String?): PlantInfo? = all.firstOrNull { it.id == id }

    fun identify(seed: Int): List<PlantMatch> {
        val first = Math.floorMod(seed, all.size)
        return listOf(88, 7, 3).mapIndexed { i, confidence ->
            PlantMatch(all[(first + i) % all.size], confidence)
        }
    }
}
