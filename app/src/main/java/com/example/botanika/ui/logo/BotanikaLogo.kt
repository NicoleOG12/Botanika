package com.example.botanika.ui.logo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint as AndroidPaint
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.GiveYouGlory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private val LogoFlowerWidth = 280.dp
private const val LOGO_TRANSITION_MILLIS = 1100
private const val LOGO_SHARED_KEY = "botanika-logo"

@Composable
internal fun BotanikaFlower(
    flower: FlowerRender,
    progress: () -> Float,
    modifier: Modifier = Modifier,
    width: Dp = LogoFlowerWidth
) {
    FlowerDrawing(
        flower = flower,
        progress = progress,
        modifier = modifier
            .width(width)
            .aspectRatio(flower.widthPx.toFloat() / flower.heightPx)
    )
}

@Composable
internal fun BotanikaName(progress: () -> Float, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.brand_name),
        color = BotanikaGreen,
        fontFamily = GiveYouGlory,
        fontSize = 56.sp,
        modifier = modifier.graphicsLayer {
            alpha = progress()
            translationY = (1f - progress()) * 24.dp.toPx()
        }
    )
}

@Composable
internal fun SharedTransitionScope.sharedLogoModifier(
    animatedVisibilityScope: AnimatedVisibilityScope
): Modifier = Modifier.sharedElement(
    sharedContentState = rememberSharedContentState(key = LOGO_SHARED_KEY),
    animatedVisibilityScope = animatedVisibilityScope,
    boundsTransform = { _, _ -> tween(LOGO_TRANSITION_MILLIS, easing = EaseInOutCubic) }
)

private class FlowerArt(
    val width: Float,
    val height: Float,
    val outline: Path,
    val xs: FloatArray,
    val ys: FloatArray,
    val radii: FloatArray,
    val order: FloatArray,
) {
    fun visibleCount(progress: Float): Int {
        var lo = 0
        var hi = order.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (order[mid] <= progress) lo = mid + 1 else hi = mid
        }
        return lo
    }
}

internal class FlowerRender private constructor(
    private val art: FlowerArt,
    val widthPx: Int,
    val heightPx: Int,
) {
    val ink: ImageBitmap
    private val maskBitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ALPHA_8)
    val mask: ImageBitmap = maskBitmap.asImageBitmap()

    private val maskCanvas = AndroidCanvas(maskBitmap)
    private val maskPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)
    private val scaleX = widthPx / art.width
    private val scaleY = heightPx / art.height
    private var drawn = 0

    init {
        val inkBitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(inkBitmap)
        canvas.scale(scaleX, scaleY)
        canvas.drawPath(
            art.outline.asAndroidPath(),
            AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply { color = BotanikaGreen.toArgb() }
        )
        ink = inkBitmap.asImageBitmap()
    }

    fun advanceTo(progress: Float) {
        val target = art.visibleCount(progress)
        if (target <= drawn) return
        for (i in drawn until target) {
            maskCanvas.drawCircle(
                art.xs[i] * scaleX,
                art.ys[i] * scaleY,
                art.radii[i] * scaleX,
                maskPaint
            )
        }
        drawn = target
    }

    companion object {
        fun load(context: Context, widthPx: Int): FlowerRender {
            val art = loadFlowerArt(context)
            val heightPx = (widthPx * art.height / art.width).roundToInt()
            return FlowerRender(art, widthPx, heightPx)
        }
    }
}

private fun loadFlowerArt(context: Context): FlowerArt {
    val res = context.resources
    val outlineData = res.openRawResource(R.raw.flower_outline).bufferedReader().use { it.readText() }
    val outline = PathParser().parsePathString(outlineData).toPath()
    outline.fillType = PathFillType.EvenOdd

    val lines = res.openRawResource(R.raw.flower_reveal).bufferedReader().use { it.readLines() }
    val (w, h) = lines[0].split(' ').map { it.toFloat() }
    val n = lines.size - 1
    val xs = FloatArray(n)
    val ys = FloatArray(n)
    val radii = FloatArray(n)
    val order = FloatArray(n)
    for (i in 0 until n) {
        val p = lines[i + 1].split(' ')
        xs[i] = p[0].toFloat()
        ys[i] = p[1].toFloat()
        radii[i] = p[2].toFloat()
        order[i] = p[3].toFloat() / 10000f
    }
    return FlowerArt(w, h, outline, xs, ys, radii, order)
}

@Composable
internal fun rememberFlowerRender(): FlowerRender? {
    val context = LocalContext.current
    val widthPx = with(LocalDensity.current) { LogoFlowerWidth.roundToPx() }
    return produceState<FlowerRender?>(initialValue = null, widthPx) {
        value = withContext(Dispatchers.Default) { FlowerRender.load(context, widthPx) }
    }.value
}

@Composable
private fun FlowerDrawing(
    flower: FlowerRender,
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }) {
        flower.advanceTo(progress())
        val dst = IntSize(size.width.roundToInt(), size.height.roundToInt())
        drawImage(flower.mask, dstSize = dst)
        drawImage(flower.ink, dstSize = dst, blendMode = BlendMode.SrcIn)
    }
}
