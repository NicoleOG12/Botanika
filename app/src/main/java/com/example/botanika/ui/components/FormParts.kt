package com.example.botanika.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaSage

private val FieldShape = RoundedCornerShape(18.dp)

@Composable
fun FormScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .gardenBackdrop()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            ScreenTopBar(title = title, onBack = onBack, trailing = trailing)
            Spacer(Modifier.height(24.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
        if (bottomBar != null) {
            val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .softShadow(shape, 16.dp)
                    .background(Color.White, shape)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                bottomBar()
            }
        }
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(
            icon = DoodleIconType.ArrowLeft,
            description = stringResource(R.string.common_back),
            onClick = onBack
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            color = BotanikaGreen,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: DoodleIconType? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BotanikaGreen,
            contentColor = Color.White,
            disabledContainerColor = BotanikaGreen.copy(alpha = 0.35f),
            disabledContentColor = Color.White
        )
    ) {
        if (icon != null) {
            DoodleIcon(icon, Modifier.size(22.dp), color = Color.White)
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DoodleIconType? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, BotanikaGreen),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = BotanikaGreen
        )
    ) {
        if (icon != null) {
            DoodleIcon(icon, Modifier.size(22.dp), color = BotanikaGreen)
            Spacer(Modifier.width(10.dp))
        }
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FormLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = BotanikaGreen,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = minLines == 1,
        minLines = minLines,
        textStyle = TextStyle(
            color = BotanikaGreen,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(BotanikaGreen),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .background(Color.White, FieldShape)
                    .border(
                        width = if (focused) 2.dp else 1.dp,
                        color = if (focused) BotanikaGreen else BotanikaOutline,
                        shape = FieldShape
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = BotanikaMuted.copy(alpha = 0.7f),
                        fontSize = 17.sp
                    )
                }
                inner()
            }
        }
    )
}

@Composable
fun OptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DoodleIconType? = null
) {
    val shape = RoundedCornerShape(50)
    val container by animateColorAsState(if (selected) BotanikaGreen else Color.White, label = "optionBg")
    val content by animateColorAsState(if (selected) Color.White else BotanikaGreen, label = "optionText")
    Row(
        modifier = modifier
            .clip(shape)
            .background(container)
            .border(1.dp, if (selected) BotanikaGreen else BotanikaOutline, shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            DoodleIcon(icon, Modifier.size(20.dp), color = content)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = content,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun PickerField(
    value: String,
    icon: DoodleIconType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color.White, FieldShape)
            .border(1.dp, BotanikaOutline, FieldShape)
            .clip(FieldShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DoodleIcon(icon, Modifier.size(22.dp), color = BotanikaMuted)
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            color = BotanikaGreen,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun NumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color.White, FieldShape)
            .border(1.dp, BotanikaOutline, FieldShape)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperButton(
            icon = DoodleIconType.Minus,
            description = stringResource(R.string.common_decrease),
            enabled = value > range.first,
            onClick = { onValueChange(value - 1) }
        )
        Text(
            text = label,
            color = BotanikaGreen,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        StepperButton(
            icon = DoodleIconType.Plus,
            description = stringResource(R.string.common_increase),
            enabled = value < range.last,
            onClick = { onValueChange(value + 1) }
        )
    }
}

@Composable
private fun StepperButton(icon: DoodleIconType, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .alpha(if (enabled) 1f else 0.35f)
            .clip(CircleShape)
            .background(BotanikaSage)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        DoodleIcon(icon, Modifier.size(20.dp))
    }
}

@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, FieldShape)
            .border(1.dp, BotanikaOutline, FieldShape)
            .clip(FieldShape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, color = BotanikaGreen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(text = subtitle, color = BotanikaMuted, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = BotanikaGreen,
                uncheckedThumbColor = BotanikaMuted,
                uncheckedTrackColor = BotanikaSage,
                uncheckedBorderColor = BotanikaOutline
            )
        )
    }
}
