package com.example.botanika.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import com.example.botanika.ui.components.bounceClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaSage

private val WaterTile = Color(0xFFE6F0FF)
private val SunTile = Color(0xFFFFF3D1)
private val FertilizerTile = Color(0xFFF7E8E0)

@Composable
internal fun CareTaskCard(
    task: CareTask,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (icon, label) = when (task.type) {
        CareType.Water -> DoodleIconType.Drop to R.string.home_task_water
        CareType.Sun -> DoodleIconType.Sun to R.string.home_task_sun
        CareType.Pesticide -> DoodleIconType.Spray to R.string.home_task_pesticide
        CareType.Fertilizer -> DoodleIconType.Fertilizer to R.string.home_task_fertilizer
    }
    val tile = when (task.type) {
        CareType.Water -> WaterTile
        CareType.Sun -> SunTile
        CareType.Pesticide -> BotanikaSage
        CareType.Fertilizer -> FertilizerTile
    }
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .then(if (onClick != null) Modifier.bounceClickable(onClick = onClick) else Modifier)
            .fillMaxWidth()
            .softShadow(shape, 6.dp)
            .background(Color.White, shape)
            .border(1.dp, BotanikaOutline, shape)
            .clip(shape)
            .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    Brush.linearGradient(listOf(Color.White, tile, tile)),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 22.dp, bottomEnd = 16.dp, bottomStart = 22.dp)
                )
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.9f),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 22.dp, bottomEnd = 16.dp, bottomStart = 22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(icon, Modifier.size(32.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = task.plant,
                color = BotanikaGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(label, task.date),
                color = BotanikaMuted,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val deleteDesc = stringResource(R.string.reminder_delete)
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BotanikaSage.copy(alpha = 0.6f))
                .semantics { contentDescription = deleteDesc }
                .clickable(role = Role.Button, onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(
                DoodleIconType.Close,
                Modifier.size(20.dp),
                color = BotanikaGreen
            )
        }
    }
}
