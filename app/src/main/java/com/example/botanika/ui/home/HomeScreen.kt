package com.example.botanika.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.components.BottomNavBar
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.MainTab
import com.example.botanika.ui.components.PlantScreenHeader
import com.example.botanika.ui.components.Sparkle
import com.example.botanika.ui.components.bounceClickable
import com.example.botanika.ui.components.gardenBackdrop
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaSage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CardGreenLight = Color(0xFF2F6B49)

private const val CONTENT_STAGGER_MILLIS = 120L
private const val CONTENT_FADE_MILLIS = 700

enum class CareType { Water, Sun, Pesticide, Fertilizer }

data class CareTask(
    val plant: String,
    val type: CareType,
    val date: String,
    val id: String = "",
    val notify: Boolean = true
)

@Composable
internal fun HomeScreen(
    userName: String = "Nicole",
    plantCount: Int = 0,
    tasks: List<CareTask> = emptyList(),
    onOpenSettings: () -> Unit = {},
    onOpenGarden: () -> Unit = {},
    onSeeAllTasks: () -> Unit = {},
    onTaskReminder: (CareTask) -> Unit = {},
    onTabSelected: (MainTab) -> Unit = {}
) {
    val sections = remember(tasks.size) { List(3 + tasks.size) { Animatable(0f) } }
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
                title = stringResource(R.string.home_greeting, userName),
                subtitle = stringResource(R.string.home_subtitle),
                onOpenSettings = onOpenSettings,
                modifier = Modifier.revealed(sections[0])
            )

            Spacer(Modifier.height(24.dp))

            PlantsCard(
                count = plantCount,
                onClick = onOpenGarden,
                modifier = Modifier.revealed(sections[1])
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .revealed(sections[2]),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_next_care),
                    color = BotanikaGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(role = Role.Button, onClick = onSeeAllTasks)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.home_see_all),
                        color = BotanikaGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(4.dp))
                    DoodleIcon(DoodleIconType.ArrowRight, Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.routine_empty),
                    color = BotanikaMuted,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }

            tasks.forEachIndexed { i, task ->
                CareTaskCard(
                    task = task,
                    onReminder = { onTaskReminder(task) },
                    modifier = Modifier.revealed(sections[3 + i])
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(16.dp))
        }

        BottomNavBar(selected = MainTab.Home, onSelected = onTabSelected)
    }
}

@Composable
private fun PlantsCard(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .bounceClickable(onClick = onClick)
            .fillMaxWidth()
            .height(172.dp)
            .softShadow(shape, 12.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(BotanikaGreen, CardGreenLight)))
    ) {
        Sparkle(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 30.dp)
                .size(14.dp),
            color = BotanikaPink
        )
        Sparkle(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 150.dp, top = 40.dp)
                .size(9.dp),
            color = Color.White,
            periodMillis = 3100,
            delayMillis = 700
        )
        Sparkle(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 26.dp, end = 22.dp)
                .size(11.dp),
            color = BotanikaPink,
            periodMillis = 2700,
            delayMillis = 1300
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(150.dp)
                .background(Color.White.copy(alpha = 0.06f), CircleShape)
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .size(90.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Column(Modifier.align(Alignment.TopStart).padding(start = 24.dp, top = 22.dp)) {
            Text(
                text = stringResource(R.string.home_your_plants),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 56.sp,
                lineHeight = 62.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        FernPlant(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp, top = 6.dp)
                .size(124.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 20.dp)
                .size(36.dp)
                .background(BotanikaSage, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(DoodleIconType.ArrowRight, Modifier.size(20.dp))
        }
    }
}
