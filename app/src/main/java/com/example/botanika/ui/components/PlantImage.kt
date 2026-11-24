package com.example.botanika.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import android.util.LruCache
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
import androidx.compose.ui.graphics.asAndroidBitmap
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
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

sealed interface PlantImage {
    data class Res(val id: Int) : PlantImage
    data class Photo(val uri: Uri) : PlantImage
    data class Url(val url: String?, val fallbackQuery: String? = null) : PlantImage
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
            val bitmap by produceState<ImageBitmap?>(initialValue = null, image.url, image.fallbackQuery) {
                value = withContext(Dispatchers.IO) { decodeUrl(image.url, image.fallbackQuery) }
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

/** Reduz a foto e a converte em data URI (JPEG/base64) para ser gravada no Firestore. */
fun photoToDataUri(context: Context, uri: Uri, maxSide: Int = 640): String? = runCatching {
    val bitmap = decodePhoto(context, uri)?.asAndroidBitmap() ?: return@runCatching null
    val scale = maxSide.toFloat() / max(bitmap.width, bitmap.height)
    val finalBitmap = if (scale < 1f) {
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    } else {
        bitmap
    }
    val out = java.io.ByteArrayOutputStream()
    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
    "data:image/jpeg;base64," + android.util.Base64.encodeToString(out.toByteArray(), android.util.Base64.NO_WRAP)
}.getOrNull()

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

private val imageMemoryCache = LruCache<String, ImageBitmap>(150)
private val PEXELS_REGEX = Regex("""pexels-photo-(\d+)""", RegexOption.IGNORE_CASE)
private val FLICKR_REGEX = Regex("""(\d{8,14})_([a-zA-Z0-9]{8,15})(_[a-z])?\.(jpg|jpeg|png)""", RegexOption.IGNORE_CASE)
private val SPECIES_PATH_REGEX = Regex("""species_image/\d+_([a-zA-Z0-9_]+)/""", RegexOption.IGNORE_CASE)

private fun resolveCandidateUrls(originalUrl: String): List<String> {
    val clean = originalUrl.trim()
    val candidates = mutableListOf<String>()

    // 1. Pexels CDN (perene, público, sem expiração)
    val pexelsMatch = PEXELS_REGEX.find(clean)
    if (pexelsMatch != null) {
        val photoId = pexelsMatch.groupValues[1]
        candidates.add("https://images.pexels.com/photos/$photoId/pexels-photo-$photoId.jpeg?auto=compress&cs=tinysrgb&w=640")
    }

    // 2. Flickr CDN (perene para fotos originárias do Flickr)
    val flickrMatch = FLICKR_REGEX.find(clean)
    if (flickrMatch != null) {
        val filename = flickrMatch.value
        val id = flickrMatch.groupValues[1]
        val secret = flickrMatch.groupValues[2]
        candidates.add("https://live.staticflickr.com/65535/$filename")
        candidates.add("https://live.staticflickr.com/65535/${id}_${secret}_b.jpg")
        candidates.add("https://live.staticflickr.com/65535/${id}_${secret}_c.jpg")
    }

    // 3. URL original completa
    candidates.add(clean)

    // 4. Versão sem query parameters assinados do S3 / Wasabi (?X-Amz-...)
    if (clean.contains("?X-Amz-") || clean.contains("&X-Amz-")) {
        val withoutQuery = clean.substringBefore("?")
        if (!candidates.contains(withoutQuery)) {
            candidates.add(withoutQuery)
        }
    }

    return candidates.distinct()
}

private fun decodeUrl(urlString: String?, fallbackQuery: String? = null): ImageBitmap? {
    val cleanUrl = urlString?.trim()?.takeIf { it.isNotBlank() }
    val cacheKey = cleanUrl ?: "fallback_${fallbackQuery?.trim()}"

    if (cleanUrl == null && fallbackQuery.isNullOrBlank()) return null

    imageMemoryCache.get(cacheKey)?.let { return it }

    if (cleanUrl != null && cleanUrl.startsWith("data:image")) {
        return runCatching {
            val bytes = android.util.Base64.decode(cleanUrl.substringAfter("base64,"), android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()?.also {
                imageMemoryCache.put(cacheKey, it)
            }
        }.getOrNull()
    }

    if (cleanUrl != null) {
        val candidates = resolveCandidateUrls(cleanUrl)
        for (candidateUrl in candidates) {
            imageMemoryCache.get(candidateUrl)?.let {
                imageMemoryCache.put(cacheKey, it)
                return it
            }

            val bitmap = fetchBitmapFromNetwork(candidateUrl)
            if (bitmap != null) {
                imageMemoryCache.put(candidateUrl, bitmap)
                imageMemoryCache.put(cacheKey, bitmap)
                return bitmap
            }
        }
    }

    // Se a imagem direta não funcionou ou era inexistente, tenta buscar a imagem oficial da espécie via Wikipedia
    val speciesQuery = fallbackQuery?.takeIf { it.isNotBlank() } ?: cleanUrl?.let { url ->
        SPECIES_PATH_REGEX.find(url)?.groupValues?.get(1)?.replace('_', ' ')
    }

    if (!speciesQuery.isNullOrBlank()) {
        val wikiImageUrl = fetchWikipediaImageUrl(speciesQuery)
        if (!wikiImageUrl.isNullOrBlank()) {
            val bitmap = fetchBitmapFromNetwork(wikiImageUrl)
            if (bitmap != null) {
                imageMemoryCache.put(cacheKey, bitmap)
                return bitmap
            }
        }
    }

    return null
}

private fun fetchWikipediaImageUrl(query: String): String? = runCatching {
    val clean = query.replace(Regex("""['"()]"""), "").trim()
    val terms = mutableListOf<String>()
    terms.add(clean.replace(' ', '_'))
    val parts = clean.split(' ').filter { it.isNotBlank() }
    if (parts.size >= 2) terms.add("${parts[0]}_${parts[1]}")
    if (parts.isNotEmpty()) terms.add(parts[0])

    for (term in terms.distinct()) {
        val encoded = java.net.URLEncoder.encode(term, "UTF-8")
        val url = URL("https://en.wikipedia.org/api/rest_v1/page/summary/$encoded")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("User-Agent", "BotanikaApp/1.0 (Mobile; Android)")
            setRequestProperty("Accept", "application/json")
        }
        if (conn.responseCode in 200..299) {
            val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val json = com.google.gson.JsonParser.parseString(jsonText).asJsonObject
            val thumb = json.getAsJsonObject("thumbnail")?.get("source")?.asString
                ?: json.getAsJsonObject("originalimage")?.get("source")?.asString
            if (!thumb.isNullOrBlank()) return@runCatching thumb
        } else {
            conn.disconnect()
        }
    }
    null
}.getOrNull()

private fun fetchBitmapFromNetwork(initialUrl: String): ImageBitmap? = runCatching {
    var currentUrl = initialUrl
    var redirects = 0
    var resultBitmap: ImageBitmap? = null

    while (redirects < 5) {
        val url = URL(currentUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 20000
            instanceFollowRedirects = true
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            )
            setRequestProperty(
                "Accept",
                "image/webp,image/apng,image/png,image/jpeg,image/*,*/*;q=0.8"
            )
        }

        val statusCode = connection.responseCode
        if (statusCode in 300..399) {
            val newLocation = connection.getHeaderField("Location")
            connection.disconnect()
            if (!newLocation.isNullOrBlank()) {
                currentUrl = if (newLocation.startsWith("http://") || newLocation.startsWith("https://")) {
                    newLocation
                } else {
                    URL(url, newLocation).toString()
                }
                redirects++
                continue
            }
        }

        if (statusCode in 200..299) {
            connection.inputStream.use { inputStream ->
                val decoded = BitmapFactory.decodeStream(inputStream)
                if (decoded != null) {
                    resultBitmap = decoded.asImageBitmap()
                }
            }
        } else {
            Log.w("PlantImage", "HTTP $statusCode ao carregar imagem: $currentUrl")
        }
        connection.disconnect()
        break
    }
    resultBitmap
}.onFailure { e ->
    Log.e("PlantImage", "Erro ao carregar imagem '$initialUrl': ${e.message}", e)
}.getOrNull()

private fun rotationDegrees(stream: InputStream): Float =
    when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
