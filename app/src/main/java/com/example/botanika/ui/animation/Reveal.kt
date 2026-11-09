package com.example.botanika.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

internal fun Modifier.revealed(progress: Animatable<Float, *>): Modifier = graphicsLayer {
    alpha = progress.value
    translationY = (1f - progress.value) * 24.dp.toPx()
}
