package com.example.botanika.data.remote

import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class DeepLApi {

    private val TAG = "DEEPLAPILOG"

    private val baseUrl = "https://api-free.deepl.com/v2/translate"
    private val apiKey = "2f83e93f-46c2-4766-9c44-192b9f2214ab:fx"

    fun traduzirParaIngles(texto: String): String {
        if (texto.isBlank()) return texto
        Log.d(TAG, "Traduzindo para inglês: $texto")
        val res = requisicaoDeepL(texto, idiomaDestino = "EN")
        return if (res.isNotBlank()) res else texto
    }

    fun traduzirParaPortugues(texto: String): String {
        if (texto.isBlank()) return texto
        Log.d(TAG, "Traduzindo para português: $texto")
        val res = requisicaoDeepL(texto, idiomaDestino = "PT")
        return if (res.isNotBlank()) res else texto
    }

    fun traduzirListaParaPortugues(textos: List<String>): List<String> {
        if (textos.isEmpty()) return emptyList()

        val indicesENaoVazios = textos.mapIndexedNotNull { index, s ->
            if (s.isNotBlank()) index to s else null
        }
        if (indicesENaoVazios.isEmpty()) return textos

        val resultado = textos.toMutableList()

        for (chunk in indicesENaoVazios.chunked(50)) {
            val listaTextos = chunk.map { it.second }
            val traducaoChunk = requisicaoDeepLLote(listaTextos, idiomaDestino = "PT")

            if (traducaoChunk.size == chunk.size) {
                chunk.forEachIndexed { i, (originalIdx, originalTexto) ->
                    val trad = traducaoChunk[i]
                    resultado[originalIdx] = if (trad.isNotBlank()) trad else originalTexto
                }
            } else {
                chunk.forEach { (originalIdx, originalTexto) ->
                    val trad = traduzirParaPortugues(originalTexto)
                    resultado[originalIdx] = if (trad.isNotBlank()) trad else originalTexto
                }
            }
        }

        return resultado
    }

    private fun requisicaoDeepLLote(textos: List<String>, idiomaDestino: String): List<String> {
        var conexao: HttpsURLConnection? = null
        try {
            val url = URI(baseUrl).toURL()
            conexao = url.openConnection() as HttpsURLConnection
            conexao.requestMethod = "POST"
            conexao.connectTimeout = 8000
            conexao.readTimeout = 8000
            conexao.doOutput = true
            conexao.doInput = true

            conexao.setRequestProperty("Authorization", "DeepL-Auth-Key $apiKey")
            conexao.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val bodyParams = StringBuilder().apply {
                textos.forEachIndexed { index, texto ->
                    if (index > 0) append("&")
                    append("text=").append(URLEncoder.encode(texto, StandardCharsets.UTF_8.toString()))
                }
                append("&target_lang=").append(idiomaDestino)
            }.toString()

            val outputStream = OutputStreamWriter(conexao.outputStream)
            outputStream.write(bodyParams)
            outputStream.flush()
            outputStream.close()

            val responseCode = conexao.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conexao.inputStream))
                val responseString = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseString.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(responseString.toString())
                val translationsArray = jsonResponse.getJSONArray("translations")
                val listaTraduzida = mutableListOf<String>()
                for (i in 0 until translationsArray.length()) {
                    listaTraduzida.add(translationsArray.getJSONObject(i).getString("text"))
                }
                return listaTraduzida
            } else {
                Log.d(TAG, "Erro na tradução em lote: Código $responseCode")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao traduzir em lote: ${e.message}", e)
        } finally {
            conexao?.disconnect()
        }
        return emptyList()
    }

    private fun requisicaoDeepL(textoParaTraduzir: String, idiomaDestino: String): String {
        var conexao: HttpsURLConnection? = null
        try {
            val url = URI(baseUrl).toURL()
            conexao = url.openConnection() as HttpsURLConnection
            conexao.requestMethod = "POST"
            conexao.connectTimeout = 5000
            conexao.readTimeout = 5000
            conexao.doOutput = true
            conexao.doInput = true

            conexao.setRequestProperty("Authorization", "DeepL-Auth-Key $apiKey")
            conexao.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val bodyParams = StringBuilder().apply {
                append("text=").append(URLEncoder.encode(textoParaTraduzir, StandardCharsets.UTF_8.toString()))
                append("&target_lang=").append(idiomaDestino)
            }.toString()

            val outputStream = OutputStreamWriter(conexao.outputStream)
            outputStream.write(bodyParams)
            outputStream.flush()
            outputStream.close()

            val responseCode = conexao.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conexao.inputStream))
                val responseString = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseString.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(responseString.toString())
                val translationsArray = jsonResponse.getJSONArray("translations")
                if (translationsArray.length() > 0) {
                    return translationsArray.getJSONObject(0).getString("text")
                }
            } else {
                Log.d(TAG, "Erro na tradução: Código $responseCode")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao traduzir: ${e.message}", e)
        } finally {
            conexao?.disconnect()
        }
        return ""
    }
}
