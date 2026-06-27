package com.minimaltask.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.minimaltask.R

class FocusNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notification = NotificationCompat.Builder(context, NotificationChannels.FOCUS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Focus completato")
            .setContentText("La sessione è terminata.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(20_000, notification)
    }
}
