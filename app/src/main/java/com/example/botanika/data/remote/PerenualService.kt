package com.example.botanika.data.remote

import android.util.Log
import com.example.botanika.ui.explore.PlantFilter

class PerenualService {

    private val plantasService: PlantasService = PlantasService()

    suspend fun plantas(busca: String, filtro: PlantFilter = PlantFilter.All): List<Planta> {
        return plantasService.buscarPlantasPerenual(busca, filtro)
    }

    suspend fun detalhes(idPlanta: Int): PlantaDetalhes? {
        Log.d("PERENUALLOG", "Buscando detalhes da planta $idPlanta no Firebase Firestore...")
        return plantasService.buscarDetalhesPlanta(idPlanta)
    }

    suspend fun detalhes(idPlanta: String): PlantaDetalhes? {
        Log.d("PERENUALLOG", "Buscando detalhes da planta $idPlanta no Firebase Firestore...")
        return plantasService.buscarDetalhesPlanta(idPlanta)
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
    val thumbnail: String?,
    val isIndoor: Boolean = false,
    val isSun: Boolean = false
)
