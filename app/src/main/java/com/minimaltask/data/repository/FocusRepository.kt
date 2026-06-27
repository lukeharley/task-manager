package com.minimaltask.data.repository

import com.minimaltask.data.database.FocusSessionDao
import com.minimaltask.data.model.FocusSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class FocusRepository @Inject constructor(
    private val focusSessionDao: FocusSessionDao
) {
    fun observeSessions(): Flow<List<FocusSession>> = focusSessionDao.observeSessions()
    fun observeTotalCompletedMinutes(): Flow<Int> = focusSessionDao.observeTotalCompletedMinutes()

    suspend fun saveSession(durationMinutes: Int, completed: Boolean) {
        focusSessionDao.insert(
            FocusSession(
                startedAt = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                completed = completed
            )
        )
    }
}
