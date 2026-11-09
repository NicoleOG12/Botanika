package com.example.botanika.ui.welcome

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.logo.BotanikaFlower
import com.example.botanika.ui.logo.FlowerRender
import com.example.botanika.ui.logo.sharedLogoModifier
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.theme.GiveYouGlory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private const val CONTENT_DELAY_MILLIS = 500L
private const val CONTENT_STAGGER_MILLIS = 180L
private const val CONTENT_FADE_MILLIS = 900
private const val DECORATION_LOOP_MILLIS = 70_000

@Composable
internal fun SharedTransitionScope.WelcomeScreen(
    flower: FlowerRender,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onStart: () -> Unit
) {
    val decoration = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val message = remember { Animatable(0f) }
    val button = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(CONTENT_DELAY_MILLIS)
        val spec = tween<Float>(CONTENT_FADE_MILLIS, easing = EaseInOutSine)
        launch { decoration.animateTo(1f, tween(CONTENT_FADE_MILLIS * 2, easing = EaseInOutSine)) }
        launch { title.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS); message.animateTo(1f, spec) }
        launch { delay(CONTENT_STAGGER_MILLIS * 2); button.animateTo(1f, spec) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BotanikaPink)
    ) {
        WelcomeDecoration(flower = flower, visibility = { decoration.value })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.weight(0.7f))

            BotanikaFlower(
                flower = flower,
                progress = { 1f },
                modifier = sharedLogoModifier(animatedVisibilityScope)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = welcomeTitle(),
                color = BotanikaGreen,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.revealed(title)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.welcome_message),
                color = BotanikaGreen,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.revealed(message)
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .revealed(button),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BotanikaGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.welcome_start),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

private class FloatingFlower(
    val x: Float,
    val y: Float,
    val size: Dp,
    val alpha: Float,
    val turns: Float,
    val bob: Dp = 0.dp,
    val phase: Float = 0f
)

private val DecorFlowers = listOf(
    FloatingFlower(x = 0.02f, y = 0.00f, size = 260.dp, alpha = 0.08f, turns = 1f),
    FloatingFlower(x = 1.00f, y = 1.00f, size = 320.dp, alpha = 0.09f, turns = -1f),
    FloatingFlower(x = 0.88f, y = 0.07f, size = 54.dp, alpha = 0.28f, turns = -2f, bob = 8.dp, phase = 0.1f),
    FloatingFlower(x = 0.10f, y = 0.945f, size = 44.dp, alpha = 0.25f, turns = 2f, bob = 6.dp, phase = 0.5f),
    FloatingFlower(x = 0.93f, y = 0.60f, size = 30.dp, alpha = 0.22f, turns = 3f, bob = 7.dp, phase = 0.8f)
)

@Composable
private fun WelcomeDecoration(flower: FlowerRender, visibility: () -> Float) {
    val loop = rememberInfiniteTransition(label = "decoration")
    val time = loop.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(DECORATION_LOOP_MILLIS, easing = LinearEasing)),
        label = "time"
    )

    Canvas(Modifier.fillMaxSize()) {
        val t = time.value
        val fade = visibility()
        DecorFlowers.forEach { f ->
            val width = f.size.roundToPx()
            val height = (width * flower.heightPx / flower.widthPx.toFloat()).roundToInt()
            val bob = f.bob.toPx() * sin(((t * 8f + f.phase) * 2f * PI).toFloat())
            val cx = size.width * f.x
            val cy = size.height * f.y + bob
            rotate(degrees = 360f * f.turns * t, pivot = Offset(cx, cy)) {
                drawImage(
                    image = flower.ink,
                    dstOffset = IntOffset((cx - width / 2f).roundToInt(), (cy - height / 2f).roundToInt()),
                    dstSize = IntSize(width, height),
                    alpha = f.alpha * fade,
                    filterQuality = FilterQuality.Medium
                )
            }
        }
    }
}

@Composable
private fun welcomeTitle() = buildAnnotatedString {
    append(stringResource(R.string.welcome_title_prefix))
    append(' ')
    withStyle(
        SpanStyle(
            fontFamily = GiveYouGlory,
            fontWeight = FontWeight.Normal,
            fontSize = 44.sp
        )
    ) {
        append(stringResource(R.string.brand_name))
    }
}
