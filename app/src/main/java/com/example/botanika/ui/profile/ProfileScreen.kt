package com.example.botanika.ui.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.botanika.R
import com.example.botanika.ui.animation.revealed
import com.example.botanika.ui.components.BottomNavBar
import com.example.botanika.ui.components.ConfirmDialog
import com.example.botanika.ui.components.DestructiveRed
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.FormTextField
import com.example.botanika.ui.components.MainTab
import com.example.botanika.ui.components.PrimaryButton
import com.example.botanika.ui.components.Sparkle
import com.example.botanika.ui.components.bounceClickable
import com.example.botanika.ui.components.formatMonthYear
import com.example.botanika.ui.components.gardenBackdrop
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.theme.BotanikaBlush
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaPink
import com.example.botanika.ui.theme.BotanikaRose
import com.example.botanika.ui.theme.BotanikaSage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CONTENT_STAGGER_MILLIS = 110L
private const val CONTENT_FADE_MILLIS = 700
private const val SECTION_COUNT = 5

private val LevelThresholds = listOf(0, 1, 3, 6, 10)
private val LevelNames = listOf(
    R.string.profile_level_0,
    R.string.profile_level_1,
    R.string.profile_level_2,
    R.string.profile_level_3,
    R.string.profile_level_4
)

private fun levelFor(plantCount: Int): Int =
    LevelThresholds.indexOfLast { plantCount >= it }.coerceAtLeast(0)

@Composable
internal fun ProfileScreen(
    userName: String,
    email: String,
    memberSinceMillis: Long?,
    plantCount: Int,
    reminderCount: Int,
    thirstyCount: Int,
    onEditName: (String) -> Unit,
    onResetPassword: () -> Unit,
    onLogout: () -> Unit,
    onTabSelected: (MainTab) -> Unit = {}
) {
    var showRename by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val sections = remember { List(SECTION_COUNT) { Animatable(0f) } }
    LaunchedEffect(sections) {
        val spec = tween<Float>(CONTENT_FADE_MILLIS, easing = EaseInOutSine)
        sections.forEachIndexed { i, a ->
            launch { delay(CONTENT_STAGGER_MILLIS * i); a.animateTo(1f, spec) }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .gardenBackdrop()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.nav_profile),
                color = BotanikaGreen,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .revealed(sections[0])
            )

            Spacer(Modifier.height(20.dp))

            ProfileHeaderCard(
                userName = userName,
                email = email,
                memberSinceMillis = memberSinceMillis,
                onEditName = { showRename = true },
                modifier = Modifier.revealed(sections[1])
            )

            Spacer(Modifier.height(16.dp))

            StatsRow(
                plantCount = plantCount,
                reminderCount = reminderCount,
                thirstyCount = thirstyCount,
                modifier = Modifier.revealed(sections[2])
            )

            Spacer(Modifier.height(16.dp))

            LevelCard(plantCount = plantCount, modifier = Modifier.revealed(sections[3]))

            Spacer(Modifier.height(28.dp))

            Column(Modifier.revealed(sections[4])) {
                Text(
                    text = stringResource(R.string.profile_section_account),
                    color = BotanikaGreen,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                ActionRow(
                    icon = DoodleIconType.Edit,
                    title = stringResource(R.string.profile_edit_name),
                    subtitle = stringResource(R.string.profile_edit_name_sub),
                    onClick = { showRename = true }
                )
                if (email.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    ActionRow(
                        icon = DoodleIconType.Lock,
                        title = stringResource(R.string.profile_change_password),
                        subtitle = stringResource(R.string.profile_change_password_sub),
                        onClick = { showResetConfirm = true }
                    )
                }
                Spacer(Modifier.height(10.dp))
                ActionRow(
                    icon = DoodleIconType.Logout,
                    title = stringResource(R.string.profile_logout),
                    subtitle = null,
                    destructive = true,
                    onClick = { showLogoutConfirm = true }
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        BottomNavBar(selected = MainTab.Profile, onSelected = onTabSelected)
    }

    if (showRename) {
        RenameDialog(
            current = userName,
            onSave = {
                showRename = false
                onEditName(it)
            },
            onDismiss = { showRename = false }
        )
    }

    if (showResetConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.profile_change_password_title),
            message = stringResource(R.string.profile_change_password_message, email),
            confirmText = stringResource(R.string.profile_change_password_confirm),
            onConfirm = {
                showResetConfirm = false
                onResetPassword()
            },
            onDismiss = { showResetConfirm = false }
        )
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.profile_logout_title),
            message = stringResource(R.string.profile_logout_message),
            confirmText = stringResource(R.string.profile_logout),
            destructive = true,
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

@Composable
private fun ProfileHeaderCard(
    userName: String,
    email: String,
    memberSinceMillis: Long?,
    onEditName: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(28.dp)
    Box(modifier = modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 8.dp)
            .clip(shape)
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.horizontalGradient(listOf(BotanikaPink, BotanikaBlush, BotanikaSage))
                )
        ) {
            Sparkle(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 28.dp, top = 22.dp)
                    .size(12.dp),
                color = Color.White
            )
            Sparkle(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 36.dp, top = 30.dp)
                    .size(16.dp),
                color = BotanikaRose.copy(alpha = 0.7f),
                periodMillis = 3000,
                delayMillis = 500
            )
            Sparkle(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 72.dp, bottom = 18.dp)
                    .size(9.dp),
                color = Color.White,
                periodMillis = 2800,
                delayMillis = 1100
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 60.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = userName,
                color = BotanikaGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (email.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = email,
                    color = BotanikaMuted,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (memberSinceMillis != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.profile_member_since, formatMonthYear(memberSinceMillis)),
                    color = BotanikaGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(BotanikaSage)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
    Avatar(
        name = userName,
        onClick = onEditName,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = 72.dp)
    )
    }
}

@Composable
private fun Avatar(name: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(modifier = modifier.size(96.dp)) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .softShadow(CircleShape, 8.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .background(Brush.linearGradient(listOf(BotanikaRose, BotanikaPink)))
                .bounceClickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .softShadow(CircleShape, 4.dp)
                .clip(CircleShape)
                .background(BotanikaGreen)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(DoodleIconType.Edit, Modifier.size(16.dp), color = Color.White)
        }
    }
}

@Composable
private fun StatsRow(
    plantCount: Int,
    reminderCount: Int,
    thirstyCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = DoodleIconType.Garden,
            value = plantCount,
            label = stringResource(R.string.profile_stat_plants),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = DoodleIconType.Calendar,
            value = reminderCount,
            label = stringResource(R.string.profile_stat_reminders),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = DoodleIconType.Drop,
            value = thirstyCount,
            label = stringResource(R.string.profile_stat_thirsty),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(icon: DoodleIconType, value: Int, label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .softShadow(shape, 6.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, BotanikaOutline, shape)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BotanikaSage),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(icon, Modifier.size(24.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = value.toString(),
            color = BotanikaGreen,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = BotanikaMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun LevelCard(plantCount: Int, modifier: Modifier = Modifier) {
    val level = levelFor(plantCount)
    val maxed = level == LevelThresholds.lastIndex
    val floor = LevelThresholds[level]
    val ceiling = if (maxed) floor else LevelThresholds[level + 1]
    val target = if (maxed) 1f else (plantCount - floor).toFloat() / (ceiling - floor)
    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(900, easing = EaseInOutSine),
        label = "levelProgress"
    )
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .softShadow(shape, 8.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(BotanikaGreen, Color(0xFF2F6B49))))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_level_label),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(LevelNames[level]),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                DoodleIcon(DoodleIconType.Garden, Modifier.size(28.dp), color = BotanikaPink)
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(50))
                    .background(BotanikaPink)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = when {
                plantCount == 0 -> stringResource(R.string.profile_level_empty)
                maxed -> stringResource(R.string.profile_level_max)
                else -> stringResource(
                    R.string.profile_level_next,
                    ceiling - plantCount,
                    stringResource(LevelNames[level + 1])
                )
            },
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ActionRow(
    icon: DoodleIconType,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    val shape = RoundedCornerShape(20.dp)
    val accent = if (destructive) DestructiveRed else BotanikaGreen
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 4.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, BotanikaOutline, shape)
            .bounceClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (destructive) DestructiveRed.copy(alpha = 0.1f) else BotanikaSage),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(icon, Modifier.size(24.dp), color = accent)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = accent,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = BotanikaMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
        DoodleIcon(DoodleIconType.ArrowRight, Modifier.size(20.dp), color = if (destructive) accent else BotanikaMuted)
    }
}

@Composable
private fun RenameDialog(current: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(current) }
    val valid = text.isNotBlank()
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_edit_name_title),
                color = BotanikaGreen,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            FormTextField(
                value = text,
                onValueChange = { text = it.take(40) },
                placeholder = stringResource(R.string.profile_edit_name_hint),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (valid) onSave(text) })
            )
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = stringResource(R.string.profile_edit_name_save),
                onClick = { onSave(text) },
                enabled = valid
            )
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = BotanikaGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
