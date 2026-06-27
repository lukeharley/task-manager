package com.minimaltask.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.minimaltask.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY priority DESC, createdAt DESC")
    fun observeActiveTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY createdAt DESC")
    fun observeCompletedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeTask(id: Int): Flow<Task?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Int): Task?

    @Query(
        """
        SELECT * FROM tasks
        WHERE completed = :completed
        AND (:query IS NULL OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND (:category IS NULL OR category = :category)
        AND (:priority IS NULL OR priority = :priority)
        ORDER BY priority DESC, createdAt DESC
        """
    )
    fun searchTasks(
        query: String?,
        category: String?,
        priority: Int?,
        completed: Boolean
    ): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1 AND completedAt BETWEEN :start AND :end")
    fun completedCountBetween(start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM tasks WHERE completed = 1")
    fun observeAllCompleted(): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY completed ASC, priority DESC, createdAt DESC")
    suspend fun getAllTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("UPDATE tasks SET completed = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun complete(id: Int, completedAt: Long)

    @Query("UPDATE tasks SET completed = 0, completedAt = NULL WHERE id = :id")
    suspend fun restore(id: Int)
}
