package com.example.botanika.ui.register

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.botanika.R
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaSage

@Composable
internal fun RegisterSuccessDialog(onContinue: () -> Unit) {
    val pop = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        check.animateTo(1f, tween(450))
    }

    Dialog(onDismissRequest = onContinue) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .scale(0.85f + 0.15f * pop.value)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .size(88.dp)
                    .scale(pop.value)
                    .clip(CircleShape)
                    .background(BotanikaSage)
            ) {
                val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                val s = size.minDimension
                val a = Offset(s * 0.28f, s * 0.52f)
                val b = Offset(s * 0.44f, s * 0.68f)
                val c = Offset(s * 0.74f, s * 0.34f)
                val firstPart = 0.35f
                val partial = Path().apply {
                    moveTo(a.x, a.y)
                    if (check.value <= firstPart) {
                        val t = check.value / firstPart
                        lineTo(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
                    } else {
                        lineTo(b.x, b.y)
                        val t = (check.value - firstPart) / (1f - firstPart)
                        lineTo(b.x + (c.x - b.x) * t, b.y + (c.y - b.y) * t)
                    }
                }
                if (check.value > 0f) drawPath(partial, BotanikaGreen, style = stroke)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.register_success_title),
                color = BotanikaGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.register_success_message),
                color = BotanikaMuted,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BotanikaGreen, contentColor = Color.White)
            ) {
                Text(
                    text = stringResource(R.string.register_success_button),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
