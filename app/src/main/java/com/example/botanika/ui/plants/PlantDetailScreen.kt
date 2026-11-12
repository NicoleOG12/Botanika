package com.example.botanika.ui.plants

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.components.BotanikaError
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.PlantImageView
import com.example.botanika.ui.components.PlantScreenBackground
import com.example.botanika.ui.components.PrimaryButton
import com.example.botanika.ui.components.RoundIconButton
import com.example.botanika.ui.components.SecondaryButton
import com.example.botanika.ui.components.daysBetween
import com.example.botanika.ui.components.formatDate
import com.example.botanika.ui.components.formatTime
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.components.todayUtcMillis
import com.example.botanika.ui.theme.BotanikaBlush
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaSage

private val HeroHeight = 330.dp
private val SheetOverlap = 36.dp

@Composable
internal fun PlantDetailScreen(
    name: String,
    image: PlantImage,
    info: PlantInfo?,
    myPlant: MyPlant?,
    reminders: List<Reminder>,
    onBack: () -> Unit,
    onAddToGarden: () -> Unit,
    onWater: () -> Unit = {},
    onCreateReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit = {},
    onRemove: () -> Unit
) {
    var confirmRemove by rememberSaveable { mutableStateOf(false) }
    val today = remember { todayUtcMillis() }

    Box(
        Modifier
            .fillMaxSize()
            .background(PlantScreenBackground)
    ) {
        PlantImageView(
            image = image,
            modifier = Modifier
                .fillMaxWidth()
                .height(HeroHeight)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(HeroHeight)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.28f),
                        0.35f to Color.Transparent
                    )
                )
        )

        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(HeroHeight - SheetOverlap))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(PlantScreenBackground)
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Text(
                        text = name,
                        color = BotanikaGreen,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (info != null) {
                        Text(
                            text = info.scientific,
                            color = BotanikaMuted,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (myPlant != null) {
                        Spacer(Modifier.height(20.dp))
                        MyPlantSummary(myPlant, today)
                    }

                    if (info != null) {
                        Spacer(Modifier.height(24.dp))
                        InfoGrid(info)

                        Spacer(Modifier.height(24.dp))
                        SectionTitle(stringResource(R.string.detail_about))
                        info.description?.let {
                            Text(
                                text = it,
                                color = BotanikaMuted,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        }

                        if (!info.tips.isNullOrEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            SectionTitle(stringResource(R.string.detail_tips))
                            info.tips.forEach { tip -> TipRow(tip) }
                        }
                    } else {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.detail_no_info),
                            color = BotanikaMuted,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    }

                    if (myPlant != null) {
                        if (myPlant.notes.isNotBlank()) {
                            Spacer(Modifier.height(24.dp))
                            SectionTitle(stringResource(R.string.detail_notes))
                            Text(
                                text = myPlant.notes,
                                color = BotanikaMuted,
                                fontSize = 16.sp,
                                lineHeight = 24.sp
                            )
                        }

                        Spacer(Modifier.height(24.dp))
                        SectionTitle(stringResource(R.string.detail_reminders))
                        if (reminders.isEmpty()) {
                            Text(
                                text = stringResource(R.string.detail_no_reminders),
                                color = BotanikaMuted,
                                fontSize = 15.sp
                            )
                        }
                        reminders.forEach { reminder ->
                            ReminderRow(
                                reminder = reminder,
                                onClick = { onEditReminder(reminder) },
                                onDelete = { onDeleteReminder(reminder) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }

                        Spacer(Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(role = Role.Button) { confirmRemove = true }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DoodleIcon(DoodleIconType.Trash, Modifier.size(20.dp), color = BotanikaError)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.detail_remove),
                                color = BotanikaError,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            ActionBar {
                if (myPlant == null) {
                    PrimaryButton(
                        text = stringResource(R.string.detail_add_to_garden),
                        onClick = onAddToGarden,
                        icon = DoodleIconType.AddPlant
                    )
                } else {
                    PrimaryButton(
                        text = stringResource(R.string.detail_create_reminder),
                        onClick = onCreateReminder,
                        icon = DoodleIconType.Bell
                    )
                }
            }
        }

        RoundIconButton(
            icon = DoodleIconType.ArrowLeft,
            description = stringResource(R.string.common_back),
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, top = 16.dp)
        )
    }

    if (confirmRemove) {
        AlertDialog(
            onDismissRequest = { confirmRemove = false },
            title = { Text(stringResource(R.string.detail_remove_title, name)) },
            text = { Text(stringResource(R.string.detail_remove_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmRemove = false
                    onRemove()
                }) { Text(stringResource(R.string.detail_remove), color = BotanikaError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmRemove = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
private fun ActionBar(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 16.dp)
            .background(Color.White, shape)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = BotanikaGreen,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun MyPlantSummary(plant: MyPlant, today: Long) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 6.dp)
            .background(Color.White, shape)
            .border(1.dp, BotanikaOutline, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryLine(stringResource(R.string.detail_location), stringResource(plant.location.label))
        SummaryLine(stringResource(R.string.detail_last_watered), formatDate(plant.lastWateredUtc))
        SummaryLine(
            stringResource(R.string.detail_next_watering),
            "${formatDate(plant.nextWateringUtc)} · ${wateringStatus(daysBetween(today, plant.nextWateringUtc))}"
        )
        SummaryLine(
            stringResource(R.string.detail_frequency),
            pluralStringResource(R.plurals.every_n_days, plant.wateringEveryDays, plant.wateringEveryDays)
        )
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            color = BotanikaMuted,
            fontSize = 14.sp,
            modifier = Modifier.width(112.dp)
        )
        Text(
            text = value,
            color = BotanikaGreen,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun wateringStatus(days: Int): String = when {
    days < 0 -> pluralStringResource(R.plurals.watering_late, -days, -days)
    days == 0 -> stringResource(R.string.watering_today)
    days == 1 -> stringResource(R.string.watering_tomorrow)
    else -> stringResource(R.string.watering_in_days, days)
}

@Composable
private fun InfoGrid(info: PlantInfo?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTile(DoodleIconType.Sun, stringResource(R.string.detail_light), info?.light?: "---", Modifier.weight(1f))
            InfoTile(DoodleIconType.Drop, stringResource(R.string.detail_water), info?.water?: "---", Modifier.weight(1f))
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTile(
                DoodleIconType.Thermometer,
                stringResource(R.string.detail_temperature),
                info?.temperature?:"---",
                Modifier.weight(1f)
            )
            InfoTile(
                DoodleIconType.Garden,
                stringResource(R.string.detail_difficulty),
                info?.difficulty?:"---",
                Modifier.weight(1f)
            )
        }
        val isPetSafe = info?.petSafe == true
        InfoTile(
            icon = if (isPetSafe) DoodleIconType.Check else DoodleIconType.Close,
            title = stringResource(R.string.detail_pets),
            value = stringResource(if (isPetSafe) R.string.detail_pets_safe else R.string.detail_pets_toxic),
            modifier = Modifier.fillMaxWidth(),
            iconColor = if (isPetSafe) BotanikaGreen else BotanikaError
        )
    }
}

@Composable
private fun InfoTile(
    icon: DoodleIconType,
    title: String,
    value: String?,
    modifier: Modifier = Modifier,
    iconColor: Color = BotanikaGreen
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(shape, 4.dp)
            .background(Color.White, shape)
            .border(1.dp, BotanikaOutline, shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BotanikaSage, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(icon, Modifier.size(24.dp), color = iconColor)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, color = BotanikaMuted, fontSize = 12.sp)
            Text(
                text = value ?: "",
                color = BotanikaGreen,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TipRow(text: String) {
    Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
                .background(BotanikaBlush, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(DoodleIconType.Check, Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            color = BotanikaMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReminderRow(
    reminder: Reminder,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape)
            .border(1.dp, BotanikaOutline, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DoodleIcon(
            when (reminder.type) {
                com.example.botanika.ui.home.CareType.Water -> DoodleIconType.Drop
                com.example.botanika.ui.home.CareType.Sun -> DoodleIconType.Sun
                com.example.botanika.ui.home.CareType.Pesticide -> DoodleIconType.Spray
                com.example.botanika.ui.home.CareType.Fertilizer -> DoodleIconType.Fertilizer
            },
            Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(reminder.type.label),
                color = BotanikaGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${formatDate(reminder.dateUtc)} · ${formatTime(reminder.hour, reminder.minute)}",
                color = BotanikaMuted,
                fontSize = 13.sp
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BotanikaSage.copy(alpha = 0.6f))
                .clickable(role = Role.Button, onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(
                DoodleIconType.Close,
                Modifier.size(16.dp),
                color = BotanikaGreen
            )
        }
    }
}
