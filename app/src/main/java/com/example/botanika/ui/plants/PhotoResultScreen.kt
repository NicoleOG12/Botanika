package com.example.botanika.ui.plants

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.FormScreen
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.PlantImageView
import com.example.botanika.ui.components.PrimaryButton
import com.example.botanika.ui.components.SecondaryButton
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.theme.BotanikaBlush
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaRose
import com.example.botanika.ui.theme.BotanikaSage
import kotlinx.coroutines.delay

private const val ANALYZE_MILLIS = 1800L

@Composable
internal fun PhotoResultScreen(
    photo: Uri,
    onBack: () -> Unit,
    onViewInfo: (PlantInfo) -> Unit,
    onAddToGarden: (PlantInfo) -> Unit
) {
    var analyzing by rememberSaveable(photo) { mutableStateOf(true) }
    var selected by rememberSaveable(photo) { mutableIntStateOf(0) }
    val matches = remember(photo) { PlantGuides.identify(photo.toString().hashCode()) }

    LaunchedEffect(photo) {
        if (analyzing) {
            delay(ANALYZE_MILLIS)
            analyzing = false
        }
    }

    val chosen = matches[selected].plant

    FormScreen(
        title = stringResource(R.string.result_title),
        onBack = onBack,
        bottomBar = {
            if (!analyzing) {
                PrimaryButton(
                    text = stringResource(R.string.result_add),
                    onClick = { onAddToGarden(chosen) },
                    icon = DoodleIconType.AddPlant
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(
                        text = stringResource(R.string.result_view_info),
                        onClick = { onViewInfo(chosen) },
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = stringResource(R.string.result_retry),
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                SecondaryButton(text = stringResource(R.string.common_cancel), onClick = onBack)
            }
        }
    ) {
        val photoShape = RoundedCornerShape(28.dp)
        PlantImageView(
            image = PlantImage.Photo(photo),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.3f)
                .softShadow(photoShape, 10.dp)
                .clip(photoShape)
        )
        Spacer(Modifier.height(24.dp))

        if (analyzing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(26.dp),
                    color = BotanikaGreen,
                    trackColor = BotanikaSage,
                    strokeWidth = 3.dp
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.result_analyzing),
                    color = BotanikaGreen,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Text(
                text = stringResource(R.string.result_hint),
                color = BotanikaMuted,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(14.dp))
            matches.forEachIndexed { index, match ->
                MatchCard(
                    match = match,
                    best = index == 0,
                    selected = index == selected,
                    onClick = { selected = index }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun MatchCard(match: PlantMatch, best: Boolean, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, if (selected) 8.dp else 3.dp)
            .background(if (selected) BotanikaBlush else Color.White, shape)
            .border(if (selected) 2.dp else 1.dp, if (selected) BotanikaRose else BotanikaOutline, shape)
            .clip(shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlantImageView(
            image = PlantImage.Res(match.plant.image),
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            if (best) {
                Text(
                    text = stringResource(R.string.result_best),
                    color = BotanikaRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(match.plant.name),
                color = BotanikaGreen,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = match.plant.scientific,
                color = BotanikaMuted,
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Spacer(Modifier.height(6.dp))
            ConfidenceBar(match.confidence)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.result_percent, match.confidence),
            color = BotanikaGreen,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConfidenceBar(percent: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(BotanikaSage)
    ) {
        Box(
            Modifier
                .fillMaxWidth(percent.coerceIn(2, 100) / 100f)
                .height(6.dp)
                .background(BotanikaGreen, RoundedCornerShape(50))
        )
    }
}
