package com.example.botanika.data.remote

import com.google.gson.annotations.SerializedName

data class PlantaDetalhes(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("common_name")
    val nomeComum: String? = null,

    @SerializedName("scientific_name")
    val nomeCientifico: List<String>? = emptyList(),

    @SerializedName("other_name")
    val outrosNomes: List<String>? = emptyList(),

    @SerializedName("family")
    val familia: String? = null,

    @SerializedName("hybrid")
    val hibrido: String? = null,

    @SerializedName("authority")
    val autoridade: String? = null,

    @SerializedName("subspecies")
    val subespecie: String? = null,

    @SerializedName("cultivar")
    val cultivar: String? = null,

    @SerializedName("variety")
    val variedade: String? = null,

    @SerializedName("species_epithet")
    val epitetosEspecie: String? = null,

    @SerializedName("genus")
    val genero: String? = null,

    @SerializedName("origin")
    val origem: List<String>? = emptyList(),

    @SerializedName("type")
    val tipo: String? = null,

    @SerializedName("dimensions")
    val dimensoes: List<DimensoesDto>? = emptyList(),

    @SerializedName("cycle")
    val ciclo: String? = null,

    @SerializedName("attracts")
    val atrai: List<String>? = emptyList(),

    @SerializedName("propagation")
    val propagacao: List<String>? = emptyList(),

    @SerializedName("hardiness")
    val resistenciaFrio: HardinessDto? = null,

    @SerializedName("hardiness_location")
    val localResistencia: HardinessLocationDto? = null,

    @SerializedName("watering")
    val rega: String? = null,

    @SerializedName("watering_general_benchmark")
    val benchmarkRega: WateringBenchmarkDto? = null,

    @SerializedName("plant_anatomy")
    val anatomiaPlanta: List<Any>? = emptyList(),

    @SerializedName("sunlight")
    val luzSolar: List<String>? = emptyList(),

    @SerializedName("pruning_month")
    val mesesPoda: List<String>? = emptyList(),

    @SerializedName("pruning_count")
    val contagemPoda: List<String>? = emptyList(),

    @SerializedName("seeds")
    val possuiSementes: Boolean = false,

    @SerializedName("maintenance")
    val manutencao: String? = null,

    @SerializedName("care_guides")
    val guiasCuidadosUrl: String? = null,

    @SerializedName("soil")
    val solo: List<String>? = emptyList(),

    @SerializedName("growth_rate")
    val taxaCrescimento: String? = null,

    @SerializedName("drought_tolerant")
    val toleranteSeca: Boolean = false,

    @SerializedName("salt_tolerant")
    val toleranteSal: Boolean = false,

    @SerializedName("thorny")
    val possuiEspinhos: Boolean = false,

    @SerializedName("invasive")
    val invasiva: Boolean = false,

    @SerializedName("tropical")
    val tropical: Boolean = false,

    @SerializedName("indoor")
    val plantaInterior: Boolean = false,

    @SerializedName("care_level")
    val nivelCuidados: String? = null,

    @SerializedName("pest_susceptibility")
    val suscetibilidadePragas: List<String>? = emptyList(),

    @SerializedName("flowers")
    val possuiFlores: Boolean = false,

    @SerializedName("flowering_season")
    val temporadaFloracao: String? = null,

    @SerializedName("cones")
    val possuiCones: Boolean = false,

    @SerializedName("fruits")
    val possuiFrutos: Boolean = false,

    @SerializedName("edible_fruit")
    val frutoComestivel: Boolean = false,

    @SerializedName("harvest_season")
    val temporadaColheita: String? = null,

    @SerializedName("leaf")
    val possuiFolhas: Boolean = false,

    @SerializedName("edible_leaf")
    val folhaComestivel: Boolean = false,

    @SerializedName("cuisine")
    val culinaria: Boolean = false,

    @SerializedName("medicinal")
    val medicinal: Boolean = false,

    @SerializedName("poisonous_to_humans")
    val venenosaHumanos: Boolean = false,

    @SerializedName("poisonous_to_pets")
    val venenosaPets: Boolean = false,

    @SerializedName("description")
    val descricao: String? = null,

    @SerializedName("default_image")
    val imagemPadrao: DefaultImageDto? = null
)

data class DimensoesDto(
    @SerializedName("type") val tipo: String? = null,
    @SerializedName("min_value") val valorMin: Double? = null,
    @SerializedName("max_value") val valorMax: Double? = null,
    @SerializedName("unit") val unidade: String? = null
)

data class HardinessDto(
    @SerializedName("min") val min: String? = null,
    @SerializedName("max") val max: String? = null
)

data class HardinessLocationDto(
    @SerializedName("full_url") val urlCompleta: String? = null,
    @SerializedName("full_iframe") val iframeCompleto: String? = null
)

data class WateringBenchmarkDto(
    @SerializedName("value") val valor: String? = null,
    @SerializedName("unit") val unidade: String? = null
)

data class DefaultImageDto(
    @SerializedName("license") val licenca: Int? = null,
    @SerializedName("license_name") val nomeLicenca: String? = null,
    @SerializedName("license_url") val urlLicenca: String? = null,
    @SerializedName("original_url") val urlOriginal: String? = null,
    @SerializedName("regular_url") val urlRegular: String? = null,
    @SerializedName("medium_url") val urlMedia: String? = null,
    @SerializedName("small_url") val urlPequena: String? = null,
    @SerializedName("thumbnail") val miniatura: String? = null
)
