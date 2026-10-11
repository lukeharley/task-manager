package com.minimaltask.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleFinish(durationMillis: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            99_001,
            Intent(context, FocusNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + durationMillis
        context.getSystemService(AlarmManager::class.java).scheduleWakeup(triggerAt, pendingIntent)
    }

    fun cancel() {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            99_001,
            Intent(context, FocusNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent)
    }
}
