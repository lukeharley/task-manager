package com.minimaltask.data.repository

import com.minimaltask.data.database.TaskDao
import com.minimaltask.data.model.Priority
import com.minimaltask.data.model.Task
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun observeActiveTasks(): Flow<List<Task>> = taskDao.observeActiveTasks()
    fun observeCompletedTasks(): Flow<List<Task>> = taskDao.observeCompletedTasks()
    fun observeTask(id: Int): Flow<Task?> = taskDao.observeTask(id)
    fun observeAllCompleted(): Flow<List<Task>> = taskDao.observeAllCompleted()
    suspend fun getAllTasks(): List<Task> = taskDao.getAllTasks()

    fun search(
        query: String?,
        category: String?,
        priority: Priority?,
        completed: Boolean = false
    ): Flow<List<Task>> = taskDao.searchTasks(
        query = query?.takeIf { it.isNotBlank() },
        category = category?.takeIf { it.isNotBlank() },
        priority = priority?.value,
        completed = completed
    )

    suspend fun getTask(id: Int): Task? = taskDao.getTask(id)
    suspend fun save(task: Task): Long = taskDao.insert(task)
    suspend fun update(task: Task) = taskDao.update(task)
    suspend fun delete(task: Task) = taskDao.delete(task)
    suspend fun complete(id: Int) = taskDao.complete(id, System.currentTimeMillis())
    suspend fun restore(id: Int) = taskDao.restore(id)
}
