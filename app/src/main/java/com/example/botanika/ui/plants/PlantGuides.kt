package com.example.botanika.ui.plants

import com.example.botanika.R

data class PlantInfo(
    val id: String,
    val name: Int,
    val tags: Int,
    val image: Int,
    val scientific: String,
    val description: String,
    val light: String,
    val water: String,
    val temperature: String,
    val difficulty: String,
    val petSafe: Boolean,
    val wateringEveryDays: Int,
    val tips: List<String>
)

data class PlantMatch(val plant: PlantInfo, val confidence: Int)

object PlantGuides {
    val all: List<PlantInfo> = listOf(
        PlantInfo(
            id = "monstera",
            name = R.string.explore_plant_monstera,
            tags = R.string.explore_tag_easy_air,
            image = R.drawable.plant_monstera,
            scientific = "Monstera deliciosa",
            description = "Planta tropical de folhas grandes e recortadas, muito usada na decoração. " +
                "Cresce bem dentro de casa, desde que tenha boa luminosidade.",
            light = "Luz indireta, bem iluminada",
            water = "A cada 7 dias",
            temperature = "18 °C a 30 °C",
            difficulty = "Fácil",
            petSafe = false,
            wateringEveryDays = 7,
            tips = listOf(
                "Regue quando os 3 cm de cima da terra estiverem secos.",
                "Limpe as folhas com um pano úmido para ajudar na respiração da planta.",
                "Ofereça um tutor para ela crescer apoiada."
            )
        ),
        PlantInfo(
            id = "kentia",
            name = R.string.explore_plant_kentia,
            tags = R.string.explore_tag_easy_air,
            image = R.drawable.plant_kentia,
            scientific = "Howea forsteriana",
            description = "Palmeira elegante e resistente, ótima para salas e escritórios. " +
                "Cresce devagar e se adapta bem a ambientes com pouca luz.",
            light = "Meia-sombra ou luz difusa",
            water = "A cada 7 dias",
            temperature = "15 °C a 28 °C",
            difficulty = "Fácil",
            petSafe = true,
            wateringEveryDays = 7,
            tips = listOf(
                "Evite sol direto: ele queima as pontas das folhas.",
                "Mantenha a terra levemente úmida, sem encharcar.",
                "Gire o vaso de vez em quando para ela crescer por igual."
            )
        ),
        PlantInfo(
            id = "zamioculcas",
            name = R.string.explore_plant_zamioculcas,
            tags = R.string.explore_tag_resistant_shade,
            image = R.drawable.plant_zamioculcas,
            scientific = "Zamioculcas zamiifolia",
            description = "Planta muito resistente, de folhas brilhantes. " +
                "Aguenta bem o esquecimento da rega e lugares com pouca luz.",
            light = "Pouca luz ou luz indireta",
            water = "A cada 14 dias",
            temperature = "18 °C a 30 °C",
            difficulty = "Muito fácil",
            petSafe = false,
            wateringEveryDays = 14,
            tips = listOf(
                "É melhor regar de menos do que de mais: o excesso apodrece as raízes.",
                "Use um vaso com furo para a água escorrer.",
                "Lave as mãos depois de mexer: a seiva pode irritar a pele."
            )
        ),
        PlantInfo(
            id = "sansevieria",
            name = R.string.explore_plant_sansevieria,
            tags = R.string.explore_tag_resistant_water,
            image = R.drawable.plant_sansevieria,
            scientific = "Dracaena trifasciata",
            description = "Também conhecida como espada-de-santa-bárbara. " +
                "Guarda água nas folhas, por isso quase não precisa de rega.",
            light = "Pleno sol ou meia-sombra",
            water = "A cada 15 dias",
            temperature = "15 °C a 35 °C",
            difficulty = "Muito fácil",
            petSafe = false,
            wateringEveryDays = 15,
            tips = listOf(
                "No inverno, regue ainda menos: uma vez por mês costuma bastar.",
                "Evite deixar água acumulada no centro das folhas.",
                "Troque de vaso só quando as raízes ocuparem todo o espaço."
            )
        ),
        PlantInfo(
            id = "aloe",
            name = R.string.explore_plant_aloe,
            tags = R.string.explore_tag_medicinal_sun,
            image = R.drawable.plant_aloe,
            scientific = "Aloe vera",
            description = "Suculenta medicinal de folhas grossas e cheias de gel. " +
                "Gosta de bastante sol e de terra que drene bem.",
            light = "Sol direto, várias horas por dia",
            water = "A cada 10 dias",
            temperature = "15 °C a 30 °C",
            difficulty = "Fácil",
            petSafe = false,
            wateringEveryDays = 10,
            tips = listOf(
                "Espere a terra secar por completo antes de regar de novo.",
                "Prefira vasos de barro, que ajudam a terra a secar.",
                "Corte as folhas mais velhas, da base, para usar o gel."
            )
        )
    )

    fun find(id: String?): PlantInfo? = all.firstOrNull { it.id == id }

    fun identify(seed: Int): List<PlantMatch> {
        val first = Math.floorMod(seed, all.size)
        return listOf(88, 7, 3).mapIndexed { i, confidence ->
            PlantMatch(all[(first + i) % all.size], confidence)
        }
    }
}
