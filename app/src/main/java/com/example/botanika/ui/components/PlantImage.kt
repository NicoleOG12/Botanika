package com.example.botanika.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.botanika.ui.theme.BotanikaMuted
import com.example.botanika.ui.theme.BotanikaSage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max

sealed interface PlantImage {
    data class Res(val id: Int) : PlantImage
    data class Photo(val uri: Uri) : PlantImage
    data class Url(val url: String) : PlantImage
    data object None : PlantImage
}

private const val MAX_PHOTO_SIDE = 1280

@Composable
fun PlantImageView(
    image: PlantImage,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when (image) {
        is PlantImage.Res -> Image(
            painter = painterResource(image.id),
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier
        )

        is PlantImage.Photo -> {
            val context = LocalContext.current
            val bitmap by produceState<ImageBitmap?>(initialValue = null, image.uri) {
                value = withContext(Dispatchers.IO) { decodePhoto(context, image.uri) }
            }
            val loaded = bitmap
            if (loaded != null) {
                Image(
                    bitmap = loaded,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = modifier
                )
            } else {
                Box(modifier.background(BotanikaSage))
            }
        }

        is PlantImage.Url -> {
            val bitmap by produceState<ImageBitmap?>(initialValue = null, image.url) {
                value = withContext(Dispatchers.IO) { decodeUrl(image.url) }
            }
            val loaded = bitmap
            if (loaded != null) {
                Image(
                    bitmap = loaded,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = modifier
                )
            } else {
                Box(modifier.background(BotanikaSage))
            }
        }

        PlantImage.None -> Box(modifier.background(BotanikaSage), contentAlignment = Alignment.Center) {
            DoodleIcon(DoodleIconType.Garden, Modifier.size(56.dp), color = BotanikaMuted)
        }
    }
}

private fun decodePhoto(context: Context, uri: Uri): ImageBitmap? = runCatching {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_PHOTO_SIDE) sample *= 2

    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
        ?: return@runCatching null

    val degrees = resolver.openInputStream(uri)?.use { rotationDegrees(it) } ?: 0f
    val result = if (degrees == 0f) {
        decoded
    } else {
        Bitmap.createBitmap(
            decoded, 0, 0, decoded.width, decoded.height,
            Matrix().apply { postRotate(degrees) }, true
        )
    }
    result.asImageBitmap()
}.getOrNull()

private fun decodeUrl(urlString: String): ImageBitmap? = runCatching {
    val url = java.net.URL(urlString)
    url.openStream().use { inputStream ->
        BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
    }
}.getOrNull()

private fun rotationDegrees(stream: InputStream): Float =
    when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
