package com.example.botanika.data.remote

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class PerenualApi {

    private val apiKey: String = "sk-gfMK6af4d5f3d7aea20554"
    private val baseUrl: String = "https://perenual.com/api/v2"


    fun buscarPlantas(busca: String?): String?{

        Log.d("PERENUALLOG", "buscando plantas: ${busca}")



        val termoCodificado = URLEncoder.encode(busca, StandardCharsets.UTF_8.toString())?: ""
        val uri = URI("${baseUrl}/species-list?key=$apiKey&q=$termoCodificado")
        val url = uri.toURL()

        Log.d("PERENUALLOG", "buscando plantas: ${url}")

        Log.d("PERENUALLOG", uri.toString())

        val response: String? = fazerRequisicaoGET(url)

        if(!response.isNullOrBlank()) return response

        return null
    }

    public fun buscarDetalhesDaPlanta(id: Int): String?{

        Log.d("PERENUALLOG", "buscando detalhes da planta")

        val uri = URI("${baseUrl}/species/details/$id?key=$apiKey")

        Log.d("PERENUALLOG", uri.toString())

        val url = uri.toURL()

        val response: String? = fazerRequisicaoGET(url)

        if(!response.isNullOrBlank()) Log.d("PERENUALLOG", response)

        return response

    }


    fun fazerRequisicaoGET(url: URL): String? {
        var conexao: HttpsURLConnection? = null
        try {
            Log.d("PERENUALLOG", "Iniciando abertura de conexão para: $url")
            conexao = url.openConnection() as HttpsURLConnection
            conexao.requestMethod = "GET"
            conexao.connectTimeout = 180000
            conexao.readTimeout = 180000
            conexao.doInput = true

            Log.d("PERENUALLOG", "Tentando conectar (connect())...")
            conexao.connect()

            Log.d("PERENUALLOG", "Conectado! Buscando responseCode...")
            val responseCode = conexao.responseCode
            Log.d("PERENUALLOG", "Código de resposta HTTP recebido: $responseCode")

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val inputStream = conexao.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val responseString = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    responseString.append(line)
                }
                reader.close()

                return responseString.toString()
            } else {
                Log.d("PERENUALLOG", "Erro na requisição: Código $responseCode")
            }

        } catch (e: Exception) {
            Log.e("PERENUALLOG", "Exceção capturada no GET: ${e.javaClass.simpleName} - ${e.message}", e)
        } finally {
            Log.d("PERENUALLOG", "Fechando conexão.")
            conexao?.disconnect()
        }

        return null
    }
}