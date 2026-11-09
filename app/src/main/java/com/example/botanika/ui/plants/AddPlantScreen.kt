package com.example.botanika.ui.plants

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.botanika.ui.components.DoodleIcon
import com.example.botanika.ui.components.DoodleIconType
import com.example.botanika.ui.components.FormScreen
import com.example.botanika.ui.components.softShadow
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaOutline
import com.example.botanika.ui.theme.BotanikaSage

@Composable
internal fun AddPlantScreen(
    onBack: () -> Unit,
    onPickFromCatalog: () -> Unit,
    onUseCamera: () -> Unit,
    onPhotoPicked: (Uri) -> Unit,
    onManual: () -> Unit
) {
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onPhotoPicked(uri) }

    FormScreen(title = stringResource(R.string.add_title), onBack = onBack) {
        Text(
            text = stringResource(R.string.add_subtitle),
            color = BotanikaMuted,
            fontSize = 16.sp,
            lineHeight = 23.sp
        )
        Spacer(Modifier.height(24.dp))

        OptionCard(
            icon = DoodleIconType.Search,
            title = stringResource(R.string.add_catalog),
            subtitle = stringResource(R.string.add_catalog_sub),
            onClick = onPickFromCatalog
        )
        Spacer(Modifier.height(14.dp))
        OptionCard(
            icon = DoodleIconType.Camera,
            title = stringResource(R.string.add_camera),
            subtitle = stringResource(R.string.add_camera_sub),
            onClick = onUseCamera
        )
        Spacer(Modifier.height(14.dp))
        OptionCard(
            icon = DoodleIconType.Image,
            title = stringResource(R.string.add_upload),
            subtitle = stringResource(R.string.add_upload_sub),
            onClick = {
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
        Spacer(Modifier.height(14.dp))
        OptionCard(
            icon = DoodleIconType.AddPlant,
            title = stringResource(R.string.add_manual),
            subtitle = stringResource(R.string.add_manual_sub),
            onClick = onManual
        )
    }
}

@Composable
private fun OptionCard(
    icon: DoodleIconType,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softShadow(shape, 8.dp)
            .background(Color.White, shape)
            .border(1.dp, BotanikaOutline, shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(BotanikaSage, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            DoodleIcon(icon, Modifier.size(30.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, color = BotanikaGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                color = BotanikaMuted,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        DoodleIcon(DoodleIconType.ArrowRight, Modifier.size(22.dp))
    }
}
