package com.example.botanika.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.botanika.MainActivity
import com.example.botanika.ui.components.addDays
import com.example.botanika.ui.components.utcDay
import com.example.botanika.ui.components.utcMonth
import com.example.botanika.ui.components.utcYear
import com.example.botanika.ui.home.CareType
import com.example.botanika.ui.plants.Reminder
import java.util.Calendar

class LembreteService {
    private val TAG = "LEMBRETE_SERVICE_LOG"

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
                plantName = reminder.plantName,
                type = reminder.type.name,
                dateUtc = reminder.dateUtc,
                hour = reminder.hour,
                minute = reminder.minute,
                repeatDays = reminder.repeatDays,
                note = reminder.note,
                notify = true,
                plantaId = reminder.plantId.toIntOrNull() ?: 0,
                nomePlanta = reminder.plantName,
                mensagem = mensagem,
                tempoEmMillis = triggerTime
            )

            dao.salvarLembrete(entity)
            Log.d(TAG, "salvarEAgendar - Lembrete salvo no Room com sucesso! ID: $reminderId")

            if (triggerTime > System.currentTimeMillis()) {
                agendarAlarme(context, reminderId.hashCode(), triggerTime, mensagem)
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

    private fun criarPendingIntent(context: Context, alarmId: Int, mensagem: String): PendingIntent {
        val intent = Intent(context, LembreteReciever::class.java).apply {
            putExtra("MENSAGEM", mensagem)
            putExtra("ID_LEMBRETE", alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun agendarAlarme(context: Context, alarmId: Int, triggerTime: Long, mensagem: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = criarPendingIntent(context, alarmId, mensagem)

            val podeExato = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
            if (podeExato) {
                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    Log.d(TAG, "agendarAlarme - Alarme exato agendado em $triggerTime")
                    return
                } catch (e: SecurityException) {
                    Log.w(TAG, "agendarAlarme - SecurityException no alarme exato: ${e.localizedMessage}")
                }
            }
            // Sem permissão de alarme exato: setAlarmClock não precisa de permissão e dispara na hora, mesmo em Doze
            val showIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerTime, showIntent), pendingIntent)
            Log.d(TAG, "agendarAlarme - Alarme agendado via setAlarmClock em $triggerTime")
        } catch (e: Exception) {
            Log.e(TAG, "EXCEÇÃO em agendarAlarme: ${e.javaClass.simpleName} - ${e.message}", e)
        }
    }

    private fun cancelarAlarme(context: Context, alarmId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(criarPendingIntent(context, alarmId, ""))
    }

    /**
     * Reagenda o próximo disparo de todos os lembretes salvos. Usado após reiniciar o celular,
     * atualizar o app e a cada notificação disparada (para lembretes recorrentes).
     */
    suspend fun reagendarTodos(context: Context) {
        val agora = System.currentTimeMillis()
        for (r in carregarLembretesDoCelular(context)) {
            val trigger = calcularTriggerTime(r)
            if (trigger > agora) {
                agendarAlarme(context, r.id.hashCode(), trigger, "Hora de cuidar de ${r.plantName} (${r.type})")
            }
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
        cancelarAlarme(context, id.hashCode())
    }
}
