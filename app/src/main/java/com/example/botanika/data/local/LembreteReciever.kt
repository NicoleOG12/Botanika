package com.example.botanika.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class LembreteReciever: BroadcastReceiver() {
    private val TAG = "LEMBRETE_RECEIVER_LOG"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive - Alarme recebido pelo BroadcastReceiver!")
        if(context == null || intent == null){
            Log.w(TAG, "onReceive - Context ou Intent é nulo.")
            return
        }

       val mensagem = intent.getStringExtra("MENSAGEM") ?: "Você tem uma tarefa!"
       val idLembrete = intent.getIntExtra("ID_LEMBRETE", 0)
       Log.d(TAG, "onReceive - idLembrete: $idLembrete, mensagem: $mensagem")

       val idChannel = "canal_botanika_alarmes"
       val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
           val channel = NotificationChannel(
               idChannel,
               "Lembretes de Plantas",
               NotificationManager.IMPORTANCE_HIGH
           ).apply {
               description = "Notificações para regar e cuidar das plantas"
           }
           notificationManager.createNotificationChannel(channel)
           Log.d(TAG, "onReceive - Canal de notificação criado/verificado: $idChannel")
        }

        val notificacao = NotificationCompat.Builder(context, idChannel)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Botanika 🌿")
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(idLembrete, notificacao)
        Log.d(TAG, "onReceive - Notificação exibida com ID: $idLembrete")
    }
}
