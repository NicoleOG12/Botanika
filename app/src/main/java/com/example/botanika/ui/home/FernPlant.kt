package com.example.botanika.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import com.example.botanika.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

private const val SWAY_PERIOD_MILLIS = 5200
private const val MAX_SWAY_DEGREES = 10.6f

private class FernPiece(val path: Path, val color: Color, val sways: Boolean, val height: Float)

private class FernArt(val width: Float, val height: Float, val pieces: List<FernPiece>)

private fun loadFern(text: String): FernArt {
    val lines = text.lines().filter { it.isNotBlank() }
    val (w, h) = lines.first().trim().split(" ").map { it.toFloat() }
    val pieces = lines.drop(1).map { line ->
        val (kind, hex, d) = line.split("|", limit = 3)
        val path = PathParser().parsePathString(d).toPath()
        val bounds = path.getBounds()
        FernPiece(
            path = path,
            color = Color(hex.removePrefix("#").toLong(16) or 0xFF000000),
            sways = kind == "A",
            height = ((FernPivot.y - bounds.center.y) / FernPivot.y).coerceIn(0f, 1f)
        )
    }
    return FernArt(w, h, pieces)
}

private val FernPivot = Offset(66f, 86f)

@Composable
private fun rememberFernArt(): State<FernArt?> {
    val context = LocalContext.current
    return produceState<FernArt?>(initialValue = null) {
        value = withContext(Dispatchers.Default) {
            loadFern(context.resources.openRawResource(R.raw.fern_plant).bufferedReader().use { it.readText() })
        }
    }
}

@Composable
internal fun FernPlant(modifier: Modifier = Modifier) {
    val art by rememberFernArt()
    val transition = rememberInfiniteTransition(label = "fernSway")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SWAY_PERIOD_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "fernPhase"
    )

    Canvas(modifier) {
        val fern = art ?: return@Canvas
        val unit = size.minDimension / fern.width
        val t = phase * 2f * PI.toFloat()
        scale(unit, unit, pivot = Offset.Zero) {
            fern.pieces.forEach { piece ->
                if (piece.sways) {
                    val h = piece.height
                    val degrees = MAX_SWAY_DEGREES * h * h.coerceAtLeast(0.2f) * 1.6f *
                        (sin(t + h * 2.4f) + 0.35f * sin(2f * t + h * 4f)) / 1.35f
                    rotate(degrees, pivot = FernPivot) { drawPath(piece.path, piece.color) }
                } else {
                    drawPath(piece.path, piece.color)
                }
            }
        }
    }
}
