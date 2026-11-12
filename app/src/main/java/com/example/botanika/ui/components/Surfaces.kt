package com.example.botanika.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.botanika.R
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaShadow

fun Modifier.softShadow(shape: Shape, elevation: Dp = 10.dp): Modifier =
    shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = BotanikaShadow.copy(alpha = 0.10f),
        spotColor = BotanikaShadow.copy(alpha = 0.22f)
    )

@Composable
fun BotanikaLoader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = BotanikaGreen,
            strokeWidth = 4.dp
        )
    }
}

@Composable
fun RoundIconButton(
    icon: DoodleIconType,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .softShadow(CircleShape, 8.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, BotanikaOutline, CircleShape)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        DoodleIcon(icon, Modifier.size(26.dp))
    }
}

@Composable
fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    RoundIconButton(
        icon = DoodleIconType.Profile,
        description = stringResource(R.string.explore_open_settings),
        onClick = onClick,
        modifier = modifier
    )
}
