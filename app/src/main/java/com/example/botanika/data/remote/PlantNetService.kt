package com.example.botanika.data.remote

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.botanika.BuildConfig
import com.example.botanika.ui.plants.PlantInfo
import com.example.botanika.ui.plants.PlantMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

enum class PlantNetErro { SemChave, SemRede, ChaveInvalida, LimiteExcedido, FotoInvalida, Desconhecido }

class PlantNetException(val erro: PlantNetErro) : Exception(erro.name)

class PlantNetService(
    private val plantasService: PlantasService = PlantasService()
) {
    private val TAG = "PLANTNET_LOG"

    suspend fun identificar(resolver: ContentResolver, foto: Uri): List<PlantMatch> {
        if (BuildConfig.PLANTNET_API_KEY.isBlank()) throw PlantNetException(PlantNetErro.SemChave)

        val json = withContext(Dispatchers.IO) {
            val bytes = lerFotoReduzida(resolver, foto) ?: throw PlantNetException(PlantNetErro.FotoInvalida)
            enviar(bytes)
        } ?: return emptyList()

        val results = json.optJSONArray("results") ?: return emptyList()
        val matches = mutableListOf<PlantMatch>()
        for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            val species = item.optJSONObject("species") ?: continue
            val cientifico = species.optString("scientificNameWithoutAuthor")
            if (cientifico.isBlank()) continue
            val comum = species.optJSONArray("commonNames")?.optString(0)?.takeIf { it.isNotBlank() }
            val imagem = item.optJSONArray("images")?.optJSONObject(0)?.optJSONObject("url")
                ?.let { it.optString("m").ifBlank { it.optString("o") } }
                ?.takeIf { it.isNotBlank() }

            val idCatalogo = plantasService.buscarIdPorNomeCientifico(cientifico)

            matches += PlantMatch(
                plant = PlantInfo(
                    id = idCatalogo ?: "",
                    name = comum,
                    tags = null,
                    image = imagem,
                    scientific = cientifico,
                    description = null,
                    light = "",
                    water = "",
                    temperature = "",
                    difficulty = null,
                    petSafe = null,
                    wateringEveryDays = null,
                    tips = null
                ),
                confidence = (item.optDouble("score", 0.0) * 100).toInt().coerceIn(0, 100)
            )
        }
        return matches
    }

    private fun enviar(foto: ByteArray): JSONObject? {
        val boundary = "----Botanika${UUID.randomUUID()}"
        val query = "api-key=${URLEncoder.encode(BuildConfig.PLANTNET_API_KEY, "UTF-8")}" +
            "&lang=pt&nb-results=3&include-related-images=true"
        val conexao = URL("https://my-api.plantnet.org/v2/identify/all?$query").openConnection() as HttpURLConnection
        try {
            conexao.requestMethod = "POST"
            conexao.connectTimeout = 30_000
            conexao.readTimeout = 60_000
            conexao.doOutput = true
            conexao.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            conexao.outputStream.use { out ->
                out.write(("--$boundary\r\nContent-Disposition: form-data; name=\"organs\"\r\n\r\nauto\r\n").toByteArray())
                out.write(
                    ("--$boundary\r\nContent-Disposition: form-data; name=\"images\"; filename=\"foto.jpg\"\r\n" +
                        "Content-Type: image/jpeg\r\n\r\n").toByteArray()
                )
                out.write(foto)
                out.write("\r\n--$boundary--\r\n".toByteArray())
            }

            return when (val code = conexao.responseCode) {
                HttpURLConnection.HTTP_OK ->
                    JSONObject(conexao.inputStream.bufferedReader().use { it.readText() })
                HttpURLConnection.HTTP_NOT_FOUND -> null
                HttpURLConnection.HTTP_UNAUTHORIZED, HttpURLConnection.HTTP_FORBIDDEN ->
                    throw PlantNetException(PlantNetErro.ChaveInvalida)
                429 -> throw PlantNetException(PlantNetErro.LimiteExcedido)
                else -> {
                    val corpo = conexao.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Pl@ntNet respondeu $code: $corpo")
                    throw PlantNetException(PlantNetErro.Desconhecido)
                }
            }
        } catch (e: PlantNetException) {
            throw e
        } catch (e: java.io.IOException) {
            Log.e(TAG, "Falha de rede no Pl@ntNet: ${e.message}", e)
            throw PlantNetException(PlantNetErro.SemRede)
        } catch (e: Exception) {
            Log.e(TAG, "Erro inesperado no Pl@ntNet: ${e.message}", e)
            throw PlantNetException(PlantNetErro.Desconhecido)
        } finally {
            conexao.disconnect()
        }
    }

    private fun lerFotoReduzida(resolver: ContentResolver, foto: Uri): ByteArray? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val stream = resolver.openInputStream(foto) ?: return null
        stream.use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

        var amostra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / amostra > MAX_LADO * 2) amostra *= 2

        val bitmap = resolver.openInputStream(foto)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = amostra })
        } ?: return null

        val escala = MAX_LADO.toFloat() / maxOf(bitmap.width, bitmap.height)
        val final = if (escala < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * escala).toInt(), (bitmap.height * escala).toInt(), true)
        } else {
            bitmap
        }
        return ByteArrayOutputStream().also { final.compress(Bitmap.CompressFormat.JPEG, 85, it) }.toByteArray()
    }

    private companion object {
        const val MAX_LADO = 1280
    }
}
