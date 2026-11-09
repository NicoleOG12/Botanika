package com.example.botanika.ui.splash

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.botanika.ui.logo.BotanikaFlower
import com.example.botanika.ui.logo.BotanikaName
import com.example.botanika.ui.logo.FlowerRender
import com.example.botanika.ui.logo.sharedLogoModifier
import com.example.botanika.ui.theme.BotanikaPink
import kotlinx.coroutines.delay

private const val FLOWER_DRAW_MILLIS = 4000
private const val TEXT_FADE_MILLIS = 1600
private const val HOLD_AFTER_TEXT_MILLIS = 1200L

@Composable
internal fun SharedTransitionScope.SplashScreen(
    flower: FlowerRender,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onFinished: () -> Unit
) {
    val flowerProgress = remember { Animatable(0f) }
    val textProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        flowerProgress.animateTo(1f, tween(FLOWER_DRAW_MILLIS, easing = EaseInOutSine))
        textProgress.animateTo(1f, tween(TEXT_FADE_MILLIS, easing = EaseInOutSine))
        delay(HOLD_AFTER_TEXT_MILLIS)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BotanikaPink),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BotanikaFlower(
                flower = flower,
                progress = { flowerProgress.value },
                modifier = sharedLogoModifier(animatedVisibilityScope)
            )
            Spacer(Modifier.height(12.dp))
            BotanikaName(progress = { textProgress.value })
        }
    }
}
