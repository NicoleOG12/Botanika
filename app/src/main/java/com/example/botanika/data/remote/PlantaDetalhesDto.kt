package com.example.botanika.data.remote

import com.google.gson.annotations.SerializedName


data class PlantaDetalhes(
    @SerializedName("id")
    val id: Int,

    @SerializedName("common_name")
    val nomeComum: String?,

    @SerializedName("scientific_name")
    val nomeCientifico: List<String>,

    @SerializedName("other_name")
    val outrosNomes: List<String>,

    @SerializedName("family")
    val familia: String?,

    @SerializedName("hybrid")
    val hibrido: String?,

    @SerializedName("authority")
    val autoridade: String?,

    @SerializedName("subspecies")
    val subespecie: String?,

    @SerializedName("cultivar")
    val cultivar: String?,

    @SerializedName("variety")
    val variedade: String?,

    @SerializedName("species_epithet")
    val epitetosEspecie: String?,

    @SerializedName("genus")
    val genero: String?,

    @SerializedName("origin")
    val origem: List<String>,

    @SerializedName("type")
    val tipo: String?,

    @SerializedName("dimensions")
    val dimensoes: List<DimensoesDto>,

    @SerializedName("cycle")
    val ciclo: String?,

    @SerializedName("attracts")
    val atrai: List<String>,

    @SerializedName("propagation")
    val propagacao: List<String>,

    @SerializedName("hardiness")
    val resistenciaFrio: HardinessDto?,

    @SerializedName("hardiness_location")
    val localResistencia: HardinessLocationDto?,

    @SerializedName("watering")
    val rega: String?,

    @SerializedName("watering_general_benchmark")
    val benchmarkRega: WateringBenchmarkDto?,

    @SerializedName("plant_anatomy")
    val anatomiaPlanta: List<Any>,

    @SerializedName("sunlight")
    val luzSolar: List<String>,

    @SerializedName("pruning_month")
    val mesesPoda: List<String>,

    @SerializedName("pruning_count")
    val contagemPoda: List<String>,

    @SerializedName("seeds")
    val possuiSementes: Boolean,

    @SerializedName("maintenance")
    val manutencao: String?,

    @SerializedName("care_guides")
    val guiasCuidadosUrl: String?,

    @SerializedName("soil")
    val solo: List<String>,

    @SerializedName("growth_rate")
    val taxaCrescimento: String?,

    @SerializedName("drought_tolerant")
    val toleranteSeca: Boolean,

    @SerializedName("salt_tolerant")
    val toleranteSal: Boolean,

    @SerializedName("thorny")
    val possuiEspinhos: Boolean,

    @SerializedName("invasive")
    val invasiva: Boolean,

    @SerializedName("tropical")
    val tropical: Boolean,

    @SerializedName("indoor")
    val plantaInterior: Boolean,

    @SerializedName("care_level")
    val nivelCuidados: String?,

    @SerializedName("pest_susceptibility")
    val suscetibilidadePragas: List<String>,

    @SerializedName("flowers")
    val possuiFlores: Boolean,

    @SerializedName("flowering_season")
    val temporadaFloracao: String?,

    @SerializedName("cones")
    val possuiCones: Boolean,

    @SerializedName("fruits")
    val possuiFrutos: Boolean,

    @SerializedName("edible_fruit")
    val frutoComestivel: Boolean,

    @SerializedName("harvest_season")
    val temporadaColheita: String?,

    @SerializedName("leaf")
    val possuiFolhas: Boolean,

    @SerializedName("edible_leaf")
    val folhaComestivel: Boolean,

    @SerializedName("cuisine")
    val culinaria: Boolean,

    @SerializedName("medicinal")
    val medicinal: Boolean,

    @SerializedName("poisonous_to_humans")
    val venenosaHumanos: Boolean,

    @SerializedName("poisonous_to_pets")
    val venenosaPets: Boolean,

    @SerializedName("description")
    val descricao: String?,

    @SerializedName("default_image")
    val imagemPadrao: DefaultImageDto?
)

data class DimensoesDto(
    @SerializedName("type") val tipo: String?,
    @SerializedName("min_value") val valorMin: Int,
    @SerializedName("max_value") val valorMax: Int,
    @SerializedName("unit") val unidade: String?
)

data class HardinessDto(
    @SerializedName("min") val min: String?,
    @SerializedName("max") val max: String?
)

data class HardinessLocationDto(
    @SerializedName("full_url") val urlCompleta: String?,
    @SerializedName("full_iframe") val iframeCompleto: String?
)

data class WateringBenchmarkDto(
    @SerializedName("value") val valor: String?,
    @SerializedName("unit") val unidade: String?
)

data class DefaultImageDto(
    @SerializedName("license") val licenca: Int,
    @SerializedName("license_name") val nomeLicenca: String?,
    @SerializedName("license_url") val urlLicenca: String?,
    @SerializedName("original_url") val urlOriginal: String?,
    @SerializedName("regular_url") val urlRegular: String?,
    @SerializedName("medium_url") val urlMedia: String?,
    @SerializedName("small_url") val urlPequena: String?,
    @SerializedName("thumbnail") val miniatura: String?
)