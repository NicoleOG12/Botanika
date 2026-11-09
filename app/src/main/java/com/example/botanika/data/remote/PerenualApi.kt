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

    private val apiKey: String = "sk-pWHU6aedb825b1d5420554"
    private val baseUrl: String = "https://perenual.com/api/v2"


    fun buscarPlantas(busca: String): String?{

        Log.d("PERENUALLOG", "buscando plantas")

        val termoCodificado = URLEncoder.encode(busca, StandardCharsets.UTF_8.toString())
        val uri = URI("${baseUrl}/species-list?key=$apiKey&q=$termoCodificado")
        val url = uri.toURL()

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

            Log.d("PERENUALLOG", "fazendo get")


            conexao = url.openConnection() as HttpsURLConnection
            conexao.requestMethod = "GET"
            conexao.connectTimeout = 8000
            conexao.readTimeout = 8000
            conexao.doInput = true


            val responseCode = conexao.responseCode
            Log.d("PERENUALLOG","Código de resposta HTTP: $responseCode")
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
                println("Erro na requisição: Código $responseCode")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conexao?.disconnect()
        }

        return null
    }
}