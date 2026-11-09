package com.example.botanika.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.botanika.ui.theme.BotanikaBackground
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.theme.BotanikaRose
import com.example.botanika.ui.theme.BotanikaSage
import kotlin.math.PI
import kotlin.math.sin

private const val PETAL_LOOP_MILLIS = 70_000

private class Petal(
    val x: Float,
    val offset: Float,
    val loops: Int,
    val radiusDp: Float,
    val spin: Float,
    val color: Color
)

private val Petals = listOf(
    Petal(0.12f, 0.05f, 1, 7f, 1f, BotanikaRose.copy(alpha = 0.26f)),
    Petal(0.78f, 0.22f, 1, 9f, -1f, BotanikaPink.copy(alpha = 0.70f)),
    Petal(0.45f, 0.41f, 2, 5f, 2f, BotanikaRose.copy(alpha = 0.20f)),
    Petal(0.90f, 0.63f, 1, 6f, 1f, BotanikaSage.copy(alpha = 0.95f)),
    Petal(0.28f, 0.78f, 1, 8f, -2f, BotanikaPink.copy(alpha = 0.65f)),
    Petal(0.62f, 0.90f, 2, 5f, 1f, BotanikaSage.copy(alpha = 0.85f))
)

private fun petalPath(r: Float): Path = Path().apply {
    moveTo(0f, -r)
    cubicTo(r * 0.95f, -r * 0.4f, r * 0.6f, r * 0.85f, 0f, r)
    cubicTo(-r * 0.6f, r * 0.85f, -r * 0.95f, -r * 0.4f, 0f, -r)
    close()
}

@Composable
fun Modifier.gardenBackdrop(): Modifier {
    val transition = rememberInfiniteTransition(label = "petals")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PETAL_LOOP_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "petalPhase"
    )
    return this
        .background(BotanikaBackground)
        .drawBehind {
            drawRect(
                Brush.radialGradient(
                    listOf(BotanikaPink.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(size.width, 0f),
                    radius = size.width * 0.95f
                )
            )
            drawRect(
                Brush.radialGradient(
                    listOf(BotanikaSage.copy(alpha = 0.85f), Color.Transparent),
                    center = Offset(0f, size.height),
                    radius = size.width * 0.9f
                )
            )
            val twoPi = 2f * PI.toFloat()
            Petals.forEach { p ->
                val progress = (phase * p.loops + p.offset) % 1f
                val sway = sin(twoPi * (phase * p.loops * 3f + p.offset)) * 18.dp.toPx()
                val r = p.radiusDp.dp.toPx()
                val cx = size.width * p.x + sway
                val cy = -r * 2f + progress * (size.height + r * 4f)
                translate(cx, cy) {
                    rotate(progress * 360f * p.spin, pivot = Offset.Zero) {
                        drawPath(petalPath(r), p.color)
                    }
                }
            }
        }
}

@Composable
fun Sparkle(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    periodMillis: Int = 2400,
    delayMillis: Int = 0
) {
    val transition = rememberInfiniteTransition(label = "sparkle")
    val twinkle by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMillis, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkleAlpha"
    )
    val path = remember { Path() }
    Canvas(modifier.graphicsLayer { alpha = twinkle; scaleX = 0.6f + 0.4f * twinkle; scaleY = 0.6f + 0.4f * twinkle }) {
        val c = size.center
        val r = size.minDimension / 2f
        val waist = r * 0.18f
        path.reset()
        path.moveTo(c.x, c.y - r)
        path.quadraticTo(c.x + waist, c.y - waist, c.x + r, c.y)
        path.quadraticTo(c.x + waist, c.y + waist, c.x, c.y + r)
        path.quadraticTo(c.x - waist, c.y + waist, c.x - r, c.y)
        path.quadraticTo(c.x - waist, c.y - waist, c.x, c.y - r)
        path.close()
        drawPath(path, color)
    }
}

fun Modifier.bounceClickable(role: Role? = Role.Button, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, role = role, onClick = onClick)
}
