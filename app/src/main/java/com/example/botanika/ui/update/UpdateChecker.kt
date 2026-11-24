package com.example.botanika.ui.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.botanika.BuildConfig
import com.example.botanika.ui.theme.BotanikaBackground
import com.example.botanika.ui.theme.BotanikaGreen
import com.example.botanika.ui.theme.BotanikaMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

private const val SITE_URL = "https://botanika-landing.vercel.app"
private const val VERSION_SOURCE_URL =
    "https://raw.githubusercontent.com/NicoleOG12/Botanika/main/app/build.gradle.kts"
private const val DOWNLOAD_URL = "$SITE_URL/#download"

private val VERSION_CODE_REGEX = Regex("""versionCode\s*=\s*(\d+)""")
private val VERSION_NAME_REGEX = Regex("""versionName\s*=\s*"([^"]*)"""")

data class UpdateInfo(val versionCode: Int, val versionName: String)

/** Lê o versionCode do build.gradle.kts na branch main do GitHub. Retorna null se estiver offline ou sem match. */
private suspend fun fetchLatestVersion(): UpdateInfo? = withContext(Dispatchers.IO) {
    runCatching {
        val conn = (URL(VERSION_SOURCE_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 5_000
            readTimeout = 5_000
            useCaches = false
        }
        try {
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
            val gradle = conn.inputStream.bufferedReader().use { it.readText() }
            val code = VERSION_CODE_REGEX.find(gradle)?.groupValues?.get(1)?.toIntOrNull()
                ?: return@runCatching null
            UpdateInfo(
                versionCode = code,
                versionName = VERSION_NAME_REGEX.find(gradle)?.groupValues?.get(1).orEmpty()
            )
        } finally {
            conn.disconnect()
        }
    }.getOrNull()
}

private fun openDownloadPage(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOAD_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
    }
}

/** Exibe um pop-up quando o site publica uma versão mais nova que a instalada. */
@Composable
fun UpdateDialog() {
    val context = LocalContext.current
    val latest by produceState<UpdateInfo?>(initialValue = null) { value = fetchLatestVersion() }
    var dismissed by remember { mutableStateOf(false) }

    val update = latest ?: return
    if (dismissed || update.versionCode <= BuildConfig.VERSION_CODE) return

    AlertDialog(
        onDismissRequest = { dismissed = true },
        containerColor = BotanikaBackground,
        title = { Text("Nova atualização disponível", color = BotanikaGreen) },
        text = {
            Column {
                val version = update.versionName.takeIf { it.isNotBlank() }?.let { " (versão $it)" }.orEmpty()
                Text("Uma nova versão do Botanika$version está disponível. Baixe o novo APK para continuar aproveitando as novidades.", color = BotanikaMuted)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                openDownloadPage(context)
                dismissed = true
            }) { Text("Baixar agora", color = BotanikaGreen) }
        },
        dismissButton = {
            TextButton(onClick = { dismissed = true }) { Text("Depois", color = BotanikaMuted) }
        }
    )
}
