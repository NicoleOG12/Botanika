package com.example.botanika.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.Int

class LembreteService {
    val TAG = "LEMBRETE_SERVICE_LOG"

    fun agendar(context: Context, idPlanta: Int, nome: String, tempo: Long, mensagem: String) {
        Log.d(TAG, "agendar - ID Planta: $idPlanta, Nome: $nome, Tempo: $tempo, Mensagem: $mensagem")
        val lembreteDao = AppDatabase.getDatabase(context).lemebreteDAO()

        CoroutineScope(Dispatchers.IO).launch {
            val novoAlarme = LembreteEntity(
                plantaId = idPlanta,
                nomePlanta = nome,
                mensagem = mensagem,
                tempoEmMillis = tempo,
                idLembrete = 0
            )

            val idGerado: Int = lembreteDao.criarLembrete(novoAlarme).toInt()
            Log.d(TAG, "agendar - Lembrete criado no Room com ID gerado: $idGerado")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, LembreteReciever::class.java).apply{
                putExtra("MENSAGEM", mensagem)
                putExtra("ID_LEMBRETE", idGerado)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                idGerado,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try{
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    tempo,
                    pendingIntent
                )
                Log.d(TAG, "agendar - Alarme exato agendado com sucesso para idLembrete: $idGerado")
            }catch (e: SecurityException){
                alarmManager.set(AlarmManager.RTC_WAKEUP, tempo, pendingIntent)
                Log.w(TAG, "agendar - SecurityException ao agendar alarme exato: ${e.localizedMessage}")
            }
        }
    }
}
