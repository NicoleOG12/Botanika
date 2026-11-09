package com.example.botanika.ui.explore

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.components.BottomNavBar
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.MainTab
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.PlantPhotoCard
import com.example.botanika.ui.components.gardenBackdrop
import com.example.botanika.ui.components.PlantScreenHeader
import com.example.botanika.ui.components.PlantSearchBar
import com.example.botanika.ui.components.normalizedForSearch
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CONTENT_STAGGER_MILLIS = 110L
private const val CONTENT_FADE_MILLIS = 700

enum class PlantFilter(val label: Int) {
    All(R.string.explore_filter_all),
    Indoor(R.string.explore_filter_indoor),
    Sun(R.string.explore_filter_sun)
}

data class CatalogPlant(
    val id: String,
    val name: Int,
    val tags: Int,
    val image: Int,
    val filters: Set<PlantFilter>
)

private val SamplePlants = listOf(
    CatalogPlant(
        "monstera", R.string.explore_plant_monstera, R.string.explore_tag_easy_air,
        R.drawable.plant_monstera, setOf(PlantFilter.Indoor)
    ),
    CatalogPlant(
        "kentia", R.string.explore_plant_kentia, R.string.explore_tag_easy_air,
        R.drawable.plant_kentia, setOf(PlantFilter.Indoor)
    ),
    CatalogPlant(
        "zamioculcas", R.string.explore_plant_zamioculcas, R.string.explore_tag_resistant_shade,
        R.drawable.plant_zamioculcas, setOf(PlantFilter.Indoor)
    ),
    CatalogPlant(
        "sansevieria", R.string.explore_plant_sansevieria, R.string.explore_tag_resistant_water,
        R.drawable.plant_sansevieria, setOf(PlantFilter.Indoor, PlantFilter.Sun)
    ),
    CatalogPlant(
        "aloe", R.string.explore_plant_aloe, R.string.explore_tag_medicinal_sun,
        R.drawable.plant_aloe, setOf(PlantFilter.Sun)
    )
)

@Composable
internal fun ExploreScreen(
    plants: List<CatalogPlant> = SamplePlants,
    onOpenSettings: () -> Unit = {},
    onCameraSearch: () -> Unit = {},
    onPlantClick: (CatalogPlant) -> Unit = {},
    onTabSelected: (MainTab) -> Unit = {}
) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(PlantFilter.All) }

    val visible = remember(plants, query, filter) {
        val q = query.trim().normalizedForSearch()
        plants.filter { plant ->
            (filter == PlantFilter.All || filter in plant.filters) &&
                (q.isEmpty() || context.getString(plant.name).normalizedForSearch().contains(q))
        }
    }

    val sections = remember(plants.size) { List(3 + plants.size) { Animatable(0f) } }
    LaunchedEffect(sections) {
        val spec = tween<Float>(CONTENT_FADE_MILLIS, easing = EaseInOutSine)
        sections.forEachIndexed { i, a ->
            launch { delay(CONTENT_STAGGER_MILLIS * i); a.animateTo(1f, spec) }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .gardenBackdrop()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            PlantScreenHeader(
                title = stringResource(R.string.explore_title),
                onOpenSettings = onOpenSettings,
                modifier = Modifier.revealed(sections[0])
            )

            Spacer(Modifier.height(20.dp))

            PlantSearchBar(
                query = query,
                onQueryChange = { query = it },
                actionIcon = DoodleIconType.Camera,
                onAction = onCameraSearch,
                actionDescription = stringResource(R.string.explore_open_camera),
                modifier = Modifier.revealed(sections[1])
            )

            Spacer(Modifier.height(10.dp))

            FilterRow(
                selected = filter,
                onSelected = { filter = it },
                modifier = Modifier.revealed(sections[2])
            )

            Spacer(Modifier.height(10.dp))

            if (visible.isEmpty()) {
                Text(
                    text = stringResource(R.string.explore_empty),
                    color = BotanikaMuted,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    textAlign = TextAlign.Center
                )
            }

            visible.forEach { plant ->
                val index = plants.indexOf(plant)
                PlantPhotoCard(
                    name = stringResource(plant.name),
                    subtitle = stringResource(plant.tags),
                    image = PlantImage.Res(plant.image),
                    onClick = { onPlantClick(plant) },
                    modifier = Modifier.revealed(sections[3 + index])
                )
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(4.dp))
        }

        BottomNavBar(selected = MainTab.Explore, onSelected = onTabSelected)
    }
}

@Composable
private fun FilterRow(
    selected: PlantFilter,
    onSelected: (PlantFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PlantFilter.entries.forEach { item ->
            FilterChip(
                text = stringResource(item.label),
                selected = item == selected,
                onClick = { onSelected(item) }
            )
        }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    val container by animateColorAsState(if (selected) BotanikaGreen else Color.White, label = "chipBg")
    val content by animateColorAsState(if (selected) Color.White else BotanikaGreen, label = "chipText")
    Box(
        modifier = Modifier
            .clip(shape)
            .background(container)
            .border(1.dp, if (selected) BotanikaGreen else BotanikaOutline, shape)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = content,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}
