package com.example.botanika.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaSage

enum class MainTab { Home, Explore, Garden, Routine, Profile }

@Composable
fun BottomNavBar(selected: MainTab, onSelected: (MainTab) -> Unit) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 16.dp)
            .background(Color.White, shape)
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        NavItem(DoodleIconType.Home, R.string.nav_home, selected == MainTab.Home) { onSelected(MainTab.Home) }
        NavItem(DoodleIconType.Search, R.string.nav_explore, selected == MainTab.Explore) { onSelected(MainTab.Explore) }
        NavItem(DoodleIconType.Garden, R.string.nav_garden, selected == MainTab.Garden) { onSelected(MainTab.Garden) }
        NavItem(DoodleIconType.Calendar, R.string.nav_routine, selected == MainTab.Routine) { onSelected(MainTab.Routine) }
        NavItem(DoodleIconType.Profile, R.string.nav_profile, selected == MainTab.Profile) { onSelected(MainTab.Profile) }
    }
}

@Composable
private fun RowScope.NavItem(icon: DoodleIconType, label: Int, selected: Boolean, onClick: () -> Unit) {
    val tint by animateColorAsState(if (selected) BotanikaGreen else BotanikaMuted, label = "navTint")
    val pill by animateColorAsState(if (selected) BotanikaSage else Color.Transparent, label = "navPill")
    val lift by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "navLift"
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(pill),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(
                type = icon,
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer {
                        val s = 1f + 0.12f * lift
                        scaleX = s
                        scaleY = s
                        translationY = -2.dp.toPx() * lift
                    },
                color = tint
            )
        }
        Text(
            text = stringResource(label),
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
