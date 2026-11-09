package com.example.botanika.ui.plants

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.botanika.R
import com.example.botanika.ui.components.BotanikaDatePickerDialog
import com.example.botanika.ui.components.BotanikaError
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.FormLabel
import com.example.botanika.ui.components.FormScreen
import com.example.botanika.ui.components.FormTextField
import com.example.botanika.ui.components.NumberStepper
import com.example.botanika.ui.components.OptionChip
import com.example.botanika.ui.components.PickerField
import com.example.botanika.ui.components.PlantImage
import com.example.botanika.ui.components.PlantImageView
import com.example.botanika.ui.components.PrimaryButton
import com.example.botanika.ui.components.addDays
import com.example.botanika.ui.components.formatDate
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.components.todayUtcMillis
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted

@Composable
internal fun PlantFormScreen(
    initial: PlantDraft,
    onBack: () -> Unit,
    onSave: (MyPlant) -> Unit
) {
    val info = PlantGuides.find(initial.catalogId)
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var photo by rememberSaveable { mutableStateOf((initial.image as? PlantImage.Photo)?.uri?.toString()) }
    var location by rememberSaveable { mutableStateOf(PlantLocation.Living) }
    var everyDays by rememberSaveable { mutableIntStateOf(info?.wateringEveryDays ?: 7) }
    var lastWatered by rememberSaveable { mutableLongStateOf(todayUtcMillis()) }
    var notes by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val currentImage: PlantImage = photo?.let { PlantImage.Photo(Uri.parse(it)) } ?: initial.image

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photo = uri.toString() }

    FormScreen(
        title = stringResource(R.string.form_title),
        onBack = onBack,
        bottomBar = {
            PrimaryButton(
                text = stringResource(R.string.form_save),
                icon = DoodleIconType.Check,
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onSave(
                            MyPlant(
                                id = "",
                                catalogId = initial.catalogId,
                                name = name.trim(),
                                image = currentImage,
                                location = location,
                                wateringEveryDays = everyDays,
                                lastWateredUtc = lastWatered,
                                notes = notes.trim()
                            )
                        )
                    }
                }
            )
        }
    ) {
        val photoShape = RoundedCornerShape(28.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.7f)
                .softShadow(photoShape, 8.dp)
                .clip(photoShape)
                .clickable(role = Role.Button) {
                    galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
        ) {
            PlantImageView(currentImage, Modifier.fillMaxWidth().aspectRatio(1.7f))
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DoodleIcon(DoodleIconType.Image, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.form_change_photo),
                    color = BotanikaGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        FormLabel(stringResource(R.string.form_name))
        FormTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = false
            },
            placeholder = stringResource(R.string.form_name_hint),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        if (nameError) {
            Text(
                text = stringResource(R.string.form_error_name),
                color = BotanikaError,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        FormLabel(stringResource(R.string.form_location))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlantLocation.entries.forEach { option ->
                OptionChip(
                    text = stringResource(option.label),
                    selected = option == location,
                    onClick = { location = option }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        FormLabel(stringResource(R.string.form_frequency))
        NumberStepper(
            value = everyDays,
            onValueChange = { everyDays = it },
            range = 1..60,
            label = pluralStringResource(R.plurals.every_n_days, everyDays, everyDays)
        )

        Spacer(Modifier.height(20.dp))

        FormLabel(stringResource(R.string.form_last_watered))
        PickerField(
            value = formatDate(lastWatered),
            icon = DoodleIconType.Calendar,
            onClick = { showDatePicker = true }
        )
        Text(
            text = stringResource(R.string.form_next_watering, formatDate(addDays(lastWatered, everyDays))),
            color = BotanikaMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )

        Spacer(Modifier.height(20.dp))

        FormLabel(stringResource(R.string.form_notes))
        FormTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = stringResource(R.string.form_notes_hint),
            minLines = 3
        )
    }

    if (showDatePicker) {
        BotanikaDatePickerDialog(
            initialUtcMillis = lastWatered,
            onDismiss = { showDatePicker = false },
            onConfirm = { lastWatered = it }
        )
    }
}
