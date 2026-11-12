package com.example.botanika.ui.garden

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.components.BottomNavBar
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.MainTab
import com.example.botanika.ui.components.PlantPhotoCard
import com.example.botanika.ui.components.gardenBackdrop
import com.example.botanika.ui.components.PlantScreenHeader
import com.example.botanika.ui.components.PlantSearchBar
import com.example.botanika.ui.components.EmptyState
import com.example.botanika.ui.components.formatDate
import com.example.botanika.ui.components.normalizedForSearch
import com.example.botanika.ui.plants.MyPlant
import com.example.botanika.ui.theme.BotanikaMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CONTENT_STAGGER_MILLIS = 110L
private const val CONTENT_FADE_MILLIS = 700

@Composable
internal fun GardenScreen(
    plants: List<MyPlant>,
    onOpenSettings: () -> Unit = {},
    onAddPlant: () -> Unit = {},
    onPlantClick: (MyPlant) -> Unit = {},
    onTabSelected: (MainTab) -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }

    val visible = remember(plants, query) {
        val q = query.trim().normalizedForSearch()
        plants.filter { q.isEmpty() || it.name.normalizedForSearch().contains(q) }
    }

    val sections = remember { List(3 + MAX_ANIMATED_CARDS) { Animatable(0f) } }
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
                title = stringResource(R.string.garden_title),
                onOpenSettings = onOpenSettings,
                modifier = Modifier.revealed(sections[0])
            )

            Spacer(Modifier.height(20.dp))

            PlantSearchBar(
                query = query,
                onQueryChange = { query = it },
                actionIcon = DoodleIconType.AddPlant,
                onAction = onAddPlant,
                actionDescription = stringResource(R.string.garden_add_plant),
                modifier = Modifier.revealed(sections[1])
            )

            Spacer(Modifier.height(20.dp))

            if (plants.isEmpty()) {
                EmptyState(
                    icon = DoodleIconType.Garden,
                    title = stringResource(R.string.garden_empty_title),
                    hint = stringResource(R.string.garden_empty_hint),
                    actionText = stringResource(R.string.garden_add_plant),
                    actionIcon = DoodleIconType.AddPlant,
                    onAction = onAddPlant,
                    modifier = Modifier.revealed(sections[2])
                )
            } else if (visible.isEmpty()) {
                EmptyState(
                    icon = DoodleIconType.Search,
                    title = stringResource(R.string.garden_empty),
                    hint = stringResource(R.string.garden_search_empty_hint)
                )
            }

            visible.forEach { plant ->
                val index = plants.indexOf(plant)
                PlantPhotoCard(
                    name = plant.name,
                    subtitle = stringResource(R.string.home_task_water, formatDate(plant.nextWateringUtc)),
                    image = plant.image,
                    onClick = { onPlantClick(plant) },
                    modifier = Modifier.revealed(sections[3 + index.coerceAtMost(MAX_ANIMATED_CARDS - 1)])
                )
                Spacer(Modifier.height(20.dp))
            }

            Spacer(Modifier.height(4.dp))
        }

        BottomNavBar(selected = MainTab.Garden, onSelected = onTabSelected)
    }
}

private const val MAX_ANIMATED_CARDS = 12
