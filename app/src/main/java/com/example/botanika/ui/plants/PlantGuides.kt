package com.example.botanika.ui.plants

import com.example.botanika.data.remote.Planta
import com.example.botanika.data.remote.PlantaDetalhes
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.todayUtcMillis

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

fun PlantaDetalhes.gerarDicasDeCuidado(): List<String> {
    val dicas = mutableListOf<String>()

    if (!solo.isNullOrEmpty()) {
        dicas.add("Solo ideal: ${solo.joinToString(", ")}")
    }
    if (!mesesPoda.isNullOrEmpty()) {
        dicas.add("Poda recomendada nos meses: ${mesesPoda.joinToString(", ")}")
    }
    if (!manutencao.isNullOrBlank()) {
        dicas.add("Nível de manutenção: $manutencao")
    }
    if (toleranteSeca) {
        dicas.add("Resistente à seca: tolera períodos com pouca água")
    }
    if (plantaInterior) {
        dicas.add("Ideal para cultivo em ambientes internos")
    }
    if (!propagacao.isNullOrEmpty()) {
        dicas.add("Propagação recomendada: ${propagacao.joinToString(", ")}")
    }
    if (benchmarkRega != null && (!benchmarkRega.valor.isNullOrBlank() || !benchmarkRega.unidade.isNullOrBlank())) {
        val benchmarkStr = listOfNotNull(benchmarkRega.valor, benchmarkRega.unidade).filter { it.isNotBlank() }.joinToString(" ")
        if (benchmarkStr.isNotBlank()) {
            dicas.add("Frequência de rega: $benchmarkStr")
        }
    }
    if (!suscetibilidadePragas.isNullOrEmpty()) {
        dicas.add("Atenção a pragas: ${suscetibilidadePragas.joinToString(", ")}")
    }
    if (venenosaPets) {
        dicas.add("Cuidado: tóxica para animais de estimação")
    }
    if (venenosaHumanos) {
        dicas.add("Atenção: não ingerir, tóxica para humanos")
    }
    if (possuiEspinhos) {
        dicas.add("Cuidado ao manusear: possui espinhos")
    }
    if (!taxaCrescimento.isNullOrBlank()) {
        dicas.add("Taxa de crescimento: $taxaCrescimento")
    }
    if (!temporadaFloracao.isNullOrBlank()) {
        dicas.add("Época de floração: $temporadaFloracao")
    }
    if (!temporadaColheita.isNullOrBlank()) {
        dicas.add("Época de colheita: $temporadaColheita")
    }

    return dicas
}

fun PlantaDetalhes.toPlantInfo(): PlantInfo = PlantInfo(
    id = id.toString(),
    name = nomeComum,
    tags = null,
    image = imagemPadrao?.urlRegular
        ?: imagemPadrao?.urlMedia
        ?: imagemPadrao?.urlOriginal
        ?: imagemPadrao?.urlPequena
        ?: imagemPadrao?.miniatura,
    scientific = nomeCientifico?.firstOrNull() ?: "",
    description = descricao,
    light = luzSolar?.firstOrNull() ?: "",
    water = rega ?: "",
    temperature = temporadaColheita ?: "",
    difficulty = nivelCuidados,
    petSafe = !venenosaPets,
    wateringEveryDays = null,
    tips = gerarDicasDeCuidado().takeIf { it.isNotEmpty() }
)

fun PlantaDetalhes.toPlanta(): Planta = Planta(
    id = id,
    nomeComum = nomeComum ?: "Nome desconhecido",
    nomeCientifico = nomeCientifico?.firstOrNull() ?: "Científico não informado",
    license = imagemPadrao?.licenca,
    licenseName = imagemPadrao?.nomeLicenca,
    licenseUrl = imagemPadrao?.urlLicenca,
    originalUrl = imagemPadrao?.urlOriginal,
    regularUrl = imagemPadrao?.urlRegular,
    mediumUrl = imagemPadrao?.urlMedia,
    smallUrl = imagemPadrao?.urlPequena,
    thumbnail = imagemPadrao?.miniatura
)

fun Planta.toMyPlant(): MyPlant = MyPlant(
    id = id.toString(),
    catalogId = id.toString(),
    name = nomeComum,
    image = PlantImage.Url(regularUrl ?: mediumUrl ?: originalUrl ?: smallUrl ?: thumbnail ?: ""),
    location = PlantLocation.entries.firstOrNull { it.name == localizacao } ?: PlantLocation.Living,
    wateringEveryDays = diasRega ?: 7,
    lastWateredUtc = ultimaRegaUtc ?: todayUtcMillis(),
    notes = notas ?: ""
)

data class PlantMatch(val plant: PlantInfo, val confidence: Int)

object PlantGuides {
    val all: List<PlantInfo> = listOf()

    fun find(id: String?): PlantInfo? = all.firstOrNull { it.id == id }

    fun identify(seed: Int): List<PlantMatch> {
        val first = Math.floorMod(seed, all.size)
        return listOf(88, 7, 3).mapIndexed { i, confidence ->
            PlantMatch(all[(first + i) % all.size], confidence)
        }
    }
}
