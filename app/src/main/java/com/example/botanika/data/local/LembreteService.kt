package com.example.botanika.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.botanika.data.remote.DeepLApi
import com.example.botanika.ui.components.addDays
import com.example.botanika.ui.components.utcDay
import com.example.botanika.ui.components.utcMonth
import com.example.botanika.ui.components.utcYear
import com.example.botanika.ui.home.CareType
import com.example.botanika.ui.plants.Reminder
import java.util.Calendar

class LembreteService {
    private val TAG = "LEMBRETE_SERVICE_LOG"

    private val deepLApi = DeepLApi()

    suspend fun salvarEAgendar(context: Context, reminder: Reminder): String {
        Log.d(TAG, "salvarEAgendar - INICIO. Planta: ${reminder.plantName}, Tipo: ${reminder.type}, ID: ${reminder.id}")
        try {
            val db = AppDatabase.getDatabase(context)
            val dao = db.lemebreteDAO()

            val reminderId = reminder.id.ifEmpty { "r_${System.currentTimeMillis()}" }
            val mensagem = "Hora de cuidar de ${reminder.plantName} (${reminder.type})"
            
            val triggerTime = calcularTriggerTime(reminder)

            val entity = LembreteEntity(
                id = reminderId,
                plantId = reminder.plantId,
                plantName = deepLApi.traduzirParaPortugues(reminder.plantName),
                type = reminder.type.name,
                dateUtc = reminder.dateUtc,
                hour = reminder.hour,
                minute = reminder.minute,
                repeatDays = reminder.repeatDays,
                note = reminder.note,
                notify = true,
                plantaId = reminder.plantId.toIntOrNull() ?: 0,
                nomePlanta = deepLApi.traduzirParaPortugues(reminder.plantName),
                mensagem = mensagem,
                tempoEmMillis = triggerTime
            )

            dao.salvarLembrete(entity)
            Log.d(TAG, "salvarEAgendar - Lembrete salvo no Room com sucesso! ID: $reminderId")

            if (triggerTime > System.currentTimeMillis()) {
                agendarAlarme(context, reminderId.hashCode(), reminder, triggerTime, mensagem)
            } else {
                Log.d(TAG, "salvarEAgendar - Lembrete no passado, alarme não agendado.")
            }

            return reminderId
        } catch (e: Exception) {
            Log.e(TAG, "EXCEÇÃO em salvarEAgendar: ${e.javaClass.simpleName} - ${e.message}", e)
            throw e
        }
    }

    private fun calcularTriggerTime(reminder: Reminder): Long {
        val year = utcYear(reminder.dateUtc)
        val month = utcMonth(reminder.dateUtc)
        val day = utcDay(reminder.dateUtc)
        val calendar = Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, reminder.hour, reminder.minute, 0)
        }
        var triggerTime = calendar.timeInMillis
        if (reminder.repeatDays > 0) {
            val now = System.currentTimeMillis()
            while (triggerTime <= now) {
                triggerTime = addDays(triggerTime, reminder.repeatDays)
            }
        }
        return triggerTime
    }

    private fun agendarAlarme(context: Context, alarmId: Int, reminder: Reminder, triggerTime: Long, mensagem: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, LembreteReciever::class.java).apply {
                putExtra("MENSAGEM", mensagem)
                putExtra("ID_LEMBRETE", alarmId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "agendarAlarme - SUCESSO: Alarme exato agendado para ${reminder.plantName} em $triggerTime")
            } catch (e: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.w(TAG, "agendarAlarme - SecurityException ao agendar alarme exato: ${e.localizedMessage}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "EXCEÇÃO em agendarAlarme: ${e.javaClass.simpleName} - ${e.message}", e)
        }
    }

    suspend fun carregarLembretesDoCelular(context: Context): List<Reminder> {
        val dao = AppDatabase.getDatabase(context).lemebreteDAO()
        val entities = dao.listarTodosLembretes()
        return entities.map { e ->
            Reminder(
                id = e.id,
                plantId = if (e.plantId.isEmpty()) e.plantaId.toString() else e.plantId,
                plantName = if (e.plantName.isEmpty()) e.nomePlanta else e.plantName,
                type = try { CareType.valueOf(e.type) } catch (_: Exception) { CareType.Water },
                dateUtc = if (e.dateUtc == 0L) e.tempoEmMillis else e.dateUtc,
                hour = e.hour,
                minute = e.minute,
                repeatDays = e.repeatDays,
                note = if (e.note.isEmpty()) e.mensagem else e.note,
                notify = true
            )
        }
    }

    suspend fun excluirLembreteDoCelular(context: Context, id: String) {
        val dao = AppDatabase.getDatabase(context).lemebreteDAO()
        dao.deletarLembretePorId(id)
    }
}
