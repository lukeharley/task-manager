package com.minimaltask.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build

/**
 * Da Android 12 gli allarmi esatti richiedono un permesso speciale che da Android 14
 * è negato di default: chiamare setExact* senza averlo lancia SecurityException.
 * In quel caso si ripiega su un allarme inesatto, che il sistema può ritardare di poco.
 */
internal fun AlarmManager.scheduleWakeup(triggerAt: Long, pendingIntent: PendingIntent) {
    val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || canScheduleExactAlarms()
    if (canExact) {
        setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    } else {
        setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }
}
