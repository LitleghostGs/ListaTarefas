package com.example.listatarefas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra("titulo") ?: "Tarefa pendente"
        val id = intent.getIntExtra("id", 0)

        // Criar canal de notificação (necessário para Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes"
            val descriptionText = "Canal para lembretes de tarefas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("tarefas_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Criar Intent para abrir o app ao clicar
        val intentApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intentApp, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, "tarefas_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lembrete de Tarefa")
            .setContentText(titulo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }
}
