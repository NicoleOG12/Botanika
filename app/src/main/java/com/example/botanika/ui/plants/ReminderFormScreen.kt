package com.example.botanika.ui.plants

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.components.BotanikaDatePickerDialog
import com.example.botanika.ui.components.BotanikaError
import com.example.botanika.ui.components.BotanikaTimePickerDialog
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.FormLabel
import com.example.botanika.ui.components.FormScreen
import com.example.botanika.ui.components.FormTextField
import com.example.botanika.ui.components.OptionChip
import com.example.botanika.ui.components.PickerField
import com.example.botanika.ui.components.PrimaryButton
import com.example.botanika.ui.components.addDays
import com.example.botanika.ui.components.formatDate
import com.example.botanika.ui.components.formatTime
import com.example.botanika.ui.components.todayUtcMillis
import com.example.botanika.ui.components.utcDay
import com.example.botanika.ui.components.utcMonth
import com.example.botanika.ui.components.utcYear
import com.example.botanika.ui.home.CareType
import com.example.botanika.ui.theme.BotanikaMuted
import java.util.Calendar

@Composable
internal fun ReminderFormScreen(
    plants: List<MyPlant>,
    existing: Reminder?,
    presetPlantId: String?,
    onBack: () -> Unit,
    onSave: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
    onAddPlant: () -> Unit
) {
    var plantId by rememberSaveable {
        mutableStateOf(existing?.plantId ?: presetPlantId ?: plants.firstOrNull()?.id)
    }
    var type by rememberSaveable { mutableStateOf(existing?.type ?: CareType.Water) }
    var date by rememberSaveable { mutableLongStateOf(existing?.dateUtc ?: todayUtcMillis()) }
    var hour by rememberSaveable { mutableIntStateOf(existing?.hour ?: 9) }
    var minute by rememberSaveable { mutableIntStateOf(existing?.minute ?: 0) }
    var repeat by rememberSaveable {
        mutableStateOf(RepeatOption.entries.firstOrNull { it.days == existing?.repeatDays } ?: RepeatOption.Never)
    }
    var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var plantError by rememberSaveable { mutableStateOf(false) }
    var dateError by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    val targetTime = remember(date, hour, minute, repeat) {
        val year = utcYear(date)
        val month = utcMonth(date)
        val day = utcDay(date)
        val cal = Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }
        var time = cal.timeInMillis
        if (repeat.days > 0) {
            val now = System.currentTimeMillis()
            while (time <= now) {
                time = addDays(time, repeat.days)
            }
        }
        time
    }
    val isPast = targetTime <= System.currentTimeMillis()

    FormScreen(
        title = stringResource(if (existing == null) R.string.reminder_title_new else R.string.reminder_title_edit),
        onBack = onBack,
        bottomBar = {
            PrimaryButton(
                text = stringResource(R.string.reminder_save),
                icon = DoodleIconType.Check,
                enabled = plants.isNotEmpty(),
                onClick = {
                    val chosen = plants.firstOrNull { it.id == plantId }
                    if (isPast) {
                        dateError = true
                    } else if (chosen == null) {
                        plantError = true
                    } else {
                        onSave(
                            Reminder(
                                id = existing?.id ?: "",
                                plantId = chosen.id,
                                plantName = chosen.name,
                                type = type,
                                dateUtc = date,
                                hour = hour,
                                minute = minute,
                                repeatDays = repeat.days,
                                note = note.trim(),
                                notify = true
                            )
                        )
                    }
                }
            )
            if (existing != null) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.reminder_delete),
                        color = BotanikaError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) {
        FormLabel(stringResource(R.string.reminder_plant))
        if (plants.isEmpty()) {
            Text(
                text = stringResource(R.string.reminder_no_plants),
                color = BotanikaMuted,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(12.dp))
            com.example.botanika.ui.components.SecondaryButton(
                text = stringResource(R.string.garden_add_plant),
                onClick = onAddPlant,
                icon = DoodleIconType.AddPlant
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                plants.forEach { plant ->
                    OptionChip(
                        text = plant.name,
                        selected = plant.id == plantId,
                        onClick = {
                            plantId = plant.id
                            plantError = false
                        }
                    )
                }
            }
            if (plantError) {
                Text(
                    text = stringResource(R.string.reminder_error_plant),
                    color = BotanikaError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        FormLabel(stringResource(R.string.reminder_type))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CareType.entries.forEach { option ->
                OptionChip(
                    text = stringResource(option.label),
                    selected = option == type,
                    onClick = { type = option },
                    icon = when (option) {
                        CareType.Water -> DoodleIconType.Drop
                        CareType.Sun -> DoodleIconType.Sun
                        CareType.Pesticide -> DoodleIconType.Spray
                        CareType.Fertilizer -> DoodleIconType.Fertilizer
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            androidx.compose.foundation.layout.Column(Modifier.weight(1.4f)) {
                FormLabel(stringResource(R.string.reminder_date))
                PickerField(
                    value = formatDate(date),
                    icon = DoodleIconType.Calendar,
                    onClick = {
                        dateError = false
                        showDatePicker = true
                    }
                )
            }
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                FormLabel(stringResource(R.string.reminder_time))
                PickerField(
                    value = formatTime(hour, minute),
                    icon = DoodleIconType.Clock,
                    onClick = {
                        dateError = false
                        showTimePicker = true
                    }
                )
            }
        }

        if (dateError) {
            Text(
                text = stringResource(R.string.reminder_error_past),
                color = BotanikaError,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        FormLabel(stringResource(R.string.reminder_repeat))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RepeatOption.entries.forEach { option ->
                OptionChip(
                    text = stringResource(option.label),
                    selected = option == repeat,
                    onClick = {
                        dateError = false
                        repeat = option
                    }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        FormLabel(stringResource(R.string.reminder_note))
        FormTextField(
            value = note,
            onValueChange = { note = it },
            placeholder = stringResource(R.string.reminder_note_hint),
            minLines = 2,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            )
        )
    }

    if (showDatePicker) {
        BotanikaDatePickerDialog(
            initialUtcMillis = date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                date = it
                dateError = false
            }
        )
    }
    if (showTimePicker) {
        BotanikaTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                hour = h
                minute = m
                dateError = false
            }
        )
    }
    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.reminder_delete_title)) },
            text = { Text(stringResource(R.string.reminder_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete(existing)
                }) { Text(stringResource(R.string.reminder_delete), color = BotanikaError, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
